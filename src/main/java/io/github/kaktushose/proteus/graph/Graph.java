package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.ProteusBuilder.ConflictStrategy;
import io.github.kaktushose.proteus.internal.ConcurrentLruCache;
import io.github.kaktushose.proteus.mapping.Flag;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.Mapper.BiMapper;
import io.github.kaktushose.proteus.mapping.Mapper.UniMapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/// Undirected, unweighted graph of all possible conversion paths.
///
/// [Type]s are the vertices of this graph. Each [Type] can have `n` neighbours. However, there cannot be multiple
/// vertices of the same [Type]. The [ConflictStrategy] is used to resolve conflicting duplicate paths.
///
/// Each edge is associated with exactly one [UniMapper].
///
/// Use [#register(Type, Type, Mapper, ConflictStrategy)] to add a new path to this graph. You can retrieve paths by
/// calling [#path(Type, Type)]. Resolved paths are cached in an LRU-Cache for future lookups.
public final class Graph {

    public record Vertex(UniMapper<Object, Object> mapper, EnumSet<Flag> flags) {}

    private final Map<Type<?>, Map<Type<?>, Vertex>> adjacencyList;
    private ConcurrentLruCache<Key, List<Edge>> pathCache;

    /// Creates a new Graph with the given cache size.
    ///
    /// @param cacheSize the cache size to use for the LRU-Cache
    public Graph(int cacheSize) {
        adjacencyList = new ConcurrentHashMap<>();
        adjustCacheSize(cacheSize);
    }

    /// Adjusts the size of the LRU-Cache. **This will create a new cache object and erase the previous one.**
    ///
    /// @param newSize the new cache size to use for the LRU-Cache
    public void adjustCacheSize(int newSize) {
        pathCache = new ConcurrentLruCache<>(newSize, this::findPath);
    }

    /// Registers a new conversion path. More formally this will add the `from` [Type] as a new vertex of this graph and
    /// the `into` [Type] as a neighbour of it and associate the given [Mapper] to this newly formed edge.
    ///
    /// @param from     the source [Type] of the conversion path
    /// @param into     the destination [Type] of the conversion path
    /// @param mapper   the [Mapper] to associate with this edge
    /// @param strategy the [ConflictStrategy] to use if the `from` [Type] is already registered as a vertex
    /// @param <S>      the type of the `from` [Type]
    /// @param <T>      the type of into `from` [Type]
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <S, T> void register(@NotNull Type<S> from,
                                @NotNull Type<T> into,
                                @NotNull Mapper<S, T> mapper,
                                @NotNull ConflictStrategy strategy,
                                @NotNull Flag... flags) {
        switch (mapper) {
            case UniMapper uniMapper -> add(from, into, uniMapper, strategy, flags);
            case BiMapper biMapper -> {
                add(from, into, biMapper::from, strategy, flags);
                add(into, from, biMapper::into, strategy, flags);
            }
        }
    }

    private void add(@NotNull Type<?> source,
                     @NotNull Type<?> target,
                     @NotNull UniMapper<Object, Object> mapper,
                     @NotNull ConflictStrategy strategy,
                     @NotNull Flag... flags) {
        Vertex present = adjacencyList.computeIfAbsent(source, _ -> new ConcurrentHashMap<>())
                .putIfAbsent(target, new Vertex(mapper, toEnumSet(flags)));
        if (present != null) {
            switch (strategy) {
                case FAIL -> throw new IllegalArgumentException(
                        "Duplicated mapper registration for route: '%s' -> '%s'".formatted(source, target)
                );
                case OVERRIDE -> adjacencyList.compute(source, (_, _) -> new ConcurrentHashMap<>())
                        .putIfAbsent(target, new Vertex(mapper, toEnumSet(flags)));
            }
        }
    }

    private EnumSet<Flag> toEnumSet(Flag... flags) {
        return flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(List.of(flags));
    }

    /// Attempts to find a path that connects the two given [Type]s. Returns an empty [List] if no path was found.
    ///
    /// @param source the source [Type] of the route
    /// @param target the destination [Type] of the route
    /// @return a possibly-empty [List] of [Edge]s that connect the `source` and `target` [Type]
    @NotNull
    public List<Edge> path(@NotNull Type<?> source, @NotNull Type<?> target) {
        return pathCache.get(new Key(source, target));
    }

    @NotNull
    private Set<Type<?>> neighbours(@NotNull Type<?> type) {
        Map<Type<?>, Vertex> mappers = adjacencyList.getOrDefault(type, Map.of());
        Set<Type<?>> result = new HashSet<>(mappers.keySet());
        result.addAll(adjacencyList.keySet().stream()
                .filter(it -> it.equalsFormat(type))
                .collect(Collectors.toSet()));

        return result;
    }

    @Nullable
    private Vertex mapper(@NotNull Type<?> from, @NotNull Type<?> into) {
        return adjacencyList.getOrDefault(from, Map.of()).get(into);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private List<Edge> findPath(@NotNull Graph.Key route) {
        Type<?> source = route.source();
        Type<?> target = route.target();

        if (source.equalsFormat(target)) {
            return path(Type.of(source.container()), Type.of(target.container()));
        }

        LinkedList<Path> queue = new LinkedList<>();
        Set<Type<?>> visited = new HashSet<>();
        queue.offer(new Path(source));
        visited.add(source);
        while (!queue.isEmpty()) {
            Path current = queue.poll();

            // if subtype or equal, simulate mapper
            if (equalsSubtype(current.head(), target)) {
                return current.addEdge(target.withStrict(true),
                        new Graph.Vertex(Mapper.uni((x, _) -> MappingResult.lossless(x)), toEnumSet())
                ).edges();
            }

            Set<Type<?>> neighbours = neighbours(current.head());
            for (Type<?> neighbour : neighbours) {
                if (visited.contains(neighbour)) {
                    continue;
                }
                visited.add(neighbour);

                var mapper = mapper(current.head(), neighbour);

                if (mapper == null) {
                    List<Edge> containerPath = path(Type.of(current.head().container()), Type.of(neighbour.container()));
                    if (containerPath.isEmpty()) continue; // no path found - skip

                    ArrayList<Edge> newEdges = new ArrayList<>(current.edges());
                    newEdges.addAll(containerPath);

                    // set enforce strict mode, because containerPath head could be subtype
                    Type<Object> lastEdgeType = newEdges.getLast().into();
                    queue.offer(new Path(newEdges, (Type<Object>) neighbour.withStrict(lastEdgeType.enforceStrictMode())));
                } else {
                    if (current.head().enforceStrictMode() && mapper.flags().contains(Flag.STRICT_SUB_TYPES)) {
                        continue;
                    }

                    Path next = current.addEdge(neighbour, mapper);
                    if (neighbour.equals(target)) {
                        return next.edges();
                    }

                    queue.offerFirst(next);
                }
            }

            if (current.head().container().type() instanceof Class<?> clazz) {
                for (Type<?> type : superTypes(clazz.getSuperclass())) {
                    queue.offerLast(current.withHead((Type<Object>) type.withStrict(true)));
                }

                for (Type<?> type : interfaces(clazz)) {
                    queue.offerLast(current.withHead((Type<Object>) type.withStrict(true)));
                }
            }
        }

        return List.of();
    }

    private Set<Type<?>> superTypes(@Nullable Class<?> clazz) {
        Set<Type<?>> superTypes = new HashSet<>();
        while (clazz != null) {
            superTypes.add(Type.of(clazz));
            clazz = clazz.getSuperclass();
        }
        return superTypes;
    }

    private Set<Type<?>> interfaces(@NotNull Class<?> clazz) {
        Set<Type<?>> interfaces = new HashSet<>();
        for (Class<?> anInterface : clazz.getInterfaces()) {
            interfaces.add(Type.of(anInterface));
            interfaces.addAll(interfaces(anInterface));
        }
        return interfaces;
    }

    private boolean equalsSubtype(Type<?> sub, Type<?> base) {
        // this is different to #equalsFormat because this returns true for Format.None
        return sub.format().equals(base.format())
        && sub.container().type() instanceof Class<?> sClass
        && base.container().type() instanceof Class<?> bClass
        && bClass.isAssignableFrom(sClass);
    }

    private record Key(@NotNull Type<?> source, @NotNull Type<?> target) {}
}
