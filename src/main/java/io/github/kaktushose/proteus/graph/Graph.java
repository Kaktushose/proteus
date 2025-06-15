package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.ProteusBuilder.ConflictStrategy;
import io.github.kaktushose.proteus.graph.Edge.UnresolvedEdge;
import io.github.kaktushose.proteus.internal.ConcurrentLruCache;
import io.github.kaktushose.proteus.mapping.Flag;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.Mapper.BiMapper;
import io.github.kaktushose.proteus.mapping.Mapper.UniMapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Format;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeReference;
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
    private ConcurrentLruCache<Pair<Type<?>, Type<?>>, List<Edge>> pathCache;

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
        return pathCache.get(new Pair<>(source, target));
    }

    @NotNull
    private Set<Type<?>> neighbours(@NotNull Type<?> type) {
        Map<Type<?>, Vertex> mappers = adjacencyList.get(type);
        Set<Type<?>> result = new HashSet<>();
        if (mappers != null) {
            result.addAll(mappers.keySet());
        }
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
    private List<Edge> findPath(@NotNull Pair<Type<?>, Type<?>> route) {
        Type<?> source = route.first();
        Type<?> target = route.second();

        if (source.equals(target)) {
            throw new IllegalArgumentException("Source and target type cannot be equal. Please report this error to the devs of proteus!");
        }

        if (source.equalsFormat(target)) {
            return List.of(new UnresolvedEdge((Type<Object>) source, (Type<Object>) target));
        }

        LinkedList<Path> queue = new LinkedList<>();
        Set<Type<?>> visited = new HashSet<>();
        queue.offer(new Path(source));
        visited.add(source);
        while (!queue.isEmpty()) {
            Path current = queue.poll();

            if (current.head().format().equals(Format.NONE) && target.format().equals(Format.NONE)
                    && target.container().type() instanceof Class<?> tClass
                    && current.head().container().type() instanceof Class<?> cClass
                    && tClass.isAssignableFrom(cClass)) {
                return current.addEdge(target, new Graph.Vertex(Mapper.uni((x, _) -> MappingResult.lossless(x)), EnumSet.noneOf(Flag.class))).edges();
            }

            Set<Type<?>> neighbours = neighbours(current.head());

            if (current.head().container().type() instanceof Class<?> clazz) {
                for (Type<?> type : superTypes(clazz.getSuperclass())) {
                    queue.offerLast(current.withHead(new Type<>(type.format(), (TypeReference<Object>) type.container(), true)));
                }

                for (Type<?> type : interfaces(clazz)) {
                    queue.offerLast(current.withHead(new Type<>(type.format(), (TypeReference<Object>) type.container(), true)));
                }
            }

            for (Type<?> neighbour : neighbours) {
                if (visited.contains(neighbour)) {
                    continue;
                }
                visited.add(neighbour);

                var mapper = mapper(current.head(), neighbour);
                if (mapper != null && current.head().enforceStrictMode() && mapper.flags().contains(Flag.STRICT_SUB_TYPES)) {
                    continue;
                }
                Path next = current.addEdge(neighbour, mapper);
                if (neighbour.equals(target) || neighbour.equalsFormat(target) || equalsSubtype(neighbour, target)) {
                    return next.edges();
                }

                queue.offerFirst(next);
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

    private boolean equalsSubtype(Type<?> neighbour, Type<?> target) {
        // this is different to #equalsFormat because this returns true for Format.None
        return neighbour.format().equals(target.format())
        && neighbour.container().type() instanceof Class<?> first
        && target.container().type() instanceof Class<?> second
        && second.isAssignableFrom(first);
    }

    private record Pair<T, U>(@NotNull T first, @NotNull U second) {}
}
