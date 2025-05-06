package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.conversion.Mapper;
import io.github.kaktushose.proteus.conversion.Mapper.BiMapper;
import io.github.kaktushose.proteus.conversion.Mapper.UniMapper;
import io.github.kaktushose.proteus.graph.Edge.UnresolvedEdge;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeAdapter;
import io.github.kaktushose.proteus.util.ConcurrentLruCache;
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
        switch ((Mapper) adapter.mapper()) {
            case UniMapper uniMapper -> add(adapter.source(), adapter.target(), uniMapper);
            case BiMapper biMapper -> {
                add(adapter.source(), adapter.target(), (UniMapper<Object, Object>) UniMapper.lossless(biMapper::from));
                add(adapter.target(), adapter.source(), (UniMapper<Object, Object>) UniMapper.lossless(biMapper::into));
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
        var mappers = adjacencyList.get(type);
        var result = new HashSet<Type<?>>();
        if (mappers != null) {
            result.addAll(mappers.keySet());
        }
        result.addAll(adjacencyList.keySet().stream()
                .filter(it -> it.equalsIgnoreContainer(type))
                .collect(Collectors.toSet()));
        return result;
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

        if (source.equalsIgnoreContainer(target)) {
            return List.of(new UnresolvedEdge((Type<Object>) source, (Type<Object>) target));
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
        return first.equalsIgnoreContainer(second);
    }

    private record Route(@NotNull Type<?> source, @NotNull Type<?> target) {}
}
