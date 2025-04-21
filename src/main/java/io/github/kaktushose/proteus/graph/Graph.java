package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.conversion.Mapper;
import io.github.kaktushose.proteus.conversion.Mapper.BiMapper;
import io.github.kaktushose.proteus.conversion.Mapper.UniMapper;
import io.github.kaktushose.proteus.graph.Edge.UnresolvedEdge;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.Type.Specific;
import io.github.kaktushose.proteus.type.TypeAdapter;
import io.github.kaktushose.proteus.util.ConcurrentLruCache;
import io.github.kaktushose.proteus.util.Helpers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Graph {

    private final Map<Type<?>, Map<Type<?>, UniMapper<Object, Object>>> adjacencyList;
    private final ConcurrentLruCache<Route, List<Edge>> pathCache;

    public Graph(int cacheSize) {
        pathCache = new ConcurrentLruCache<>(cacheSize, this::findPath);
        adjacencyList = new ConcurrentHashMap<>();
    }

    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Graph register(@NotNull TypeAdapter<?, ?> adapter) {
        if (Helpers.invalidRoute(adapter.source().getClass(), adapter.target().getClass())) {
            throw new IllegalArgumentException("Cannot mix different types!");
        }
        switch ((Mapper) adapter.mapper()) {
            case UniMapper uniMapper -> add(adapter.source(), adapter.target(), uniMapper);
            case BiMapper biMapper -> {
                add(adapter.source(), adapter.target(), biMapper::from);
                add(adapter.target(), adapter.source(), biMapper::into);
            }
        }
        return this;
    }

    private void add(@NotNull Type<?> source, @NotNull Type<?> target, @NotNull UniMapper<Object, Object> adapter) {
        var present = adjacencyList.computeIfAbsent(source, unused -> new HashMap<>()).putIfAbsent(target, adapter);
        if (present != null) {
            throw new IllegalArgumentException("Duplicate adapter registration");
        }
    }

    @NotNull
    public List<Edge> path(@NotNull Type<?> source, @NotNull Type<?> target) {
        return pathCache.get(new Route(source, target));
    }

    @NotNull
    public Set<Type<?>> neighbours(@NotNull Type<?> type) {
        if (type instanceof Specific<?> specific) {
            var mappers = adjacencyList.get(type);
            var result = new HashSet<Type<?>>();
            if (mappers != null) {
                result.addAll(mappers.keySet());
            }
            result.addAll(adjacencyList.keySet().stream()
                    .filter(Specific.class::isInstance)
                    .filter(it -> ((Specific<?>) it).equalsIgnoreContainer(specific))
                    .collect(Collectors.toSet()));
            return result;
        }
        return adjacencyList.getOrDefault(type, Map.of()).keySet();
    }

    @Nullable
    public UniMapper<?, ?> mapper(@NotNull Type<?> from, @NotNull Type<?> into) {
        var mappers = adjacencyList.getOrDefault(from, Map.of());
        return mappers.get(into);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private List<Edge> findPath(@NotNull Route route) {
        var source = route.source;
        var target = route.target;

        if (source.equals(target)) {
            return List.of();
        }

        if (source instanceof Specific<?> from && target instanceof Specific<?> into && from.equalsIgnoreContainer(into)) {
            return List.of(new UnresolvedEdge((Specific<Object>) from, (Specific<Object>) into));
        }

        Queue<Path> queue = new LinkedList<>();
        Set<Type<?>> visited = new HashSet<>();
        queue.offer(new Path(source));
        visited.add(source);
        while (!queue.isEmpty()) {
            Path path = queue.poll();
            for (Type<?> neighbour : neighbours(path.head())) {
                if (visited.contains(neighbour)) {
                    continue;
                }
                visited.add(neighbour);

                Path newPath = path.addEdge(neighbour, mapper(path.head(), neighbour));
                if (equals(neighbour, target)) {
                    return newPath.edges();
                }
                queue.offer(newPath);
            }
        }
        return List.of();
    }

    private boolean equals(@NotNull Type<?> first, @NotNull Type<?> second) {
        if (first.equals(second)) {
            return true;
        }
        if (first instanceof Specific<?> from && second instanceof Specific<?> into) {
            return from.equalsIgnoreContainer(into);
        }
        return false;
    }

    private record Route(@NotNull Type<?> source, @NotNull Type<?> target) {}
}
