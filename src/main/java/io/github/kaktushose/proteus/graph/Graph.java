package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.ProteusBuilder.ConflictStrategy;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.Mapper.BiMapper;
import io.github.kaktushose.proteus.mapping.Mapper.UniMapper;
import io.github.kaktushose.proteus.graph.Edge.UnresolvedEdge;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.util.ConcurrentLruCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Graph {

    private final Map<Type<?>, Map<Type<?>, UniMapper<Object, Object>>> adjacencyList;
    private ConcurrentLruCache<Route, List<Edge>> pathCache;

    public Graph(int cacheSize) {
        adjacencyList = new ConcurrentHashMap<>();
        adjustCacheSize(cacheSize);
    }

    public void adjustCacheSize(int newSize) {
        pathCache = new ConcurrentLruCache<>(newSize, this::findPath);
    }

    @NotNull
    public <S, T> Graph register(@NotNull Type<S> from, @NotNull Type<T> into, @NotNull Mapper<S, T> mapper) {
        return register(from, into, mapper, ConflictStrategy.OVERRIDE);
    }

    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <S, T> Graph register(@NotNull Type<S> from, @NotNull Type<T> into, @NotNull Mapper<S, T> mapper, @NotNull ConflictStrategy strategy) {
        switch (mapper) {
            case UniMapper uniMapper -> add(from, into, uniMapper, strategy);
            case BiMapper biMapper -> {
                add(from, into, (UniMapper<Object, Object>) Mapper.lossless(biMapper::from), strategy);
                add(from, into, (UniMapper<Object, Object>) Mapper.lossless(biMapper::into), strategy);
            }
        }
        return this;
    }

    private void add(@NotNull Type<?> source, @NotNull Type<?> target, @NotNull UniMapper<Object, Object> adapter, @NotNull ConflictStrategy strategy) {
        UniMapper<Object, Object> present = adjacencyList.computeIfAbsent(source, unused -> new ConcurrentHashMap<>()).putIfAbsent(target, adapter);
        if (present != null) {
            switch (strategy) {
                case FAIL -> throw new IllegalArgumentException("Duplicated adapter registration for route: '%s' -> '%s'".formatted(source, target));
                case OVERRIDE -> adjacencyList.compute(source, (k, v) -> new ConcurrentHashMap<>()).putIfAbsent(target, adapter);
            }
        }
    }

    @NotNull
    public List<Edge> path(@NotNull Type<?> source, @NotNull Type<?> target) {
        return pathCache.get(new Route(source, target));
    }

    @NotNull
    private Set<Type<?>> neighbours(@NotNull Type<?> type) {
        Map<Type<?>, UniMapper<Object, Object>> mappers = adjacencyList.get(type);
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
    private UniMapper<?, ?> mapper(@NotNull Type<?> from, @NotNull Type<?> into) {
        return adjacencyList.getOrDefault(from, Map.of()).get(into);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private List<Edge> findPath(@NotNull Route route) {
        Type<?> source = route.source;
        Type<?> target = route.target;

        if (source.equals(target)) {
            return List.of();
        }

        if (source.equalsFormat(target)) {
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
        return first.equalsFormat(second);
    }

    private record Route(@NotNull Type<?> source, @NotNull Type<?> target) {}
}
