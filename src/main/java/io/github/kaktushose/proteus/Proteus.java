package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.adapter.ReversibleTypAdapter;
import io.github.kaktushose.proteus.adapter.TypeAdapter;
import io.github.kaktushose.proteus.util.ConcurrentLruCache;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Proteus {

    private final Map<Class<?>, Map<Class<?>, Function<Object, Optional<Object>>>> adjacencyList;
    private final ConcurrentLruCache<Route, List<Class<?>>> pathCache;

    public Proteus() {
        this(1000);
    }

    public Proteus(int cacheCapacity) {
        adjacencyList = new ConcurrentHashMap<>();
        pathCache = new ConcurrentLruCache<>(cacheCapacity, this::findPath);
    }

    @NotNull
    private List<Class<?>> findPath(@NotNull Route route) {
        var source = route.source;
        var target = route.target;
        if (source.equals(target)) {
            return List.of(source);
        }

        Queue<List<Class<?>>> queue = new LinkedList<>();
        Set<Class<?>> visited = new HashSet<>();
        queue.offer(List.of(source));
        visited.add(source);

        while (!queue.isEmpty()) {
            List<Class<?>> path = queue.poll();
            Class<?> node = path.getLast();

            for (Class<?> neighbour : adjacencyList.getOrDefault(node, Map.of()).keySet()) {
                if (visited.contains(neighbour)) {
                    continue;
                }
                visited.add(neighbour);

                List<Class<?>> newPath = new ArrayList<>(path);
                newPath.add(neighbour);
                if (neighbour.equals(target)) {
                    return newPath;
                }
                queue.offer(newPath);
            }
        }
        return List.of();
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <S, T> Proteus register(@NotNull Class<S> source, @NotNull Class<T> target, @NotNull TypeAdapter<S, T> adapter) {
        adjacencyList.computeIfAbsent(source, unused -> new HashMap<>()).put(target, (TypeAdapter<Object, Object>) adapter);

        if (adapter instanceof ReversibleTypAdapter<S, T> reversibleTypAdapter) {
            adjacencyList.computeIfAbsent(target, unused -> new HashMap<>()).put(
                    source,
                    value -> ((ReversibleTypAdapter<Object, Object>) reversibleTypAdapter).reverse(value)
            );
        }
        return this;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <S, T> Proteus register(@NotNull Class<S> source,
                                   @NotNull Class<T> target,
                                   @NotNull Function<S, Optional<T>> from,
                                   @NotNull Function<T, Optional<S>> into) {
        adjacencyList.computeIfAbsent(source, unused -> new HashMap<>()).put(
                target,
                value -> Optional.ofNullable(from.apply((S) value))
        );
        adjacencyList.computeIfAbsent(target, unused -> new HashMap<>()).put(
                source,
                value -> Optional.ofNullable(into.apply((T) value))
        );
        return this;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <S, T> Optional<T> convert(@NotNull S value, @NotNull Class<T> target) {
        var path = pathCache.get(new Route(value.getClass(), target));
        if (path.isEmpty()) {
            return Optional.empty();
        }

        Optional<Object> intermediate = Optional.of(value);
        for (int i = 0; i < path.size() - 1; i++) {
            Class<?> from = path.get(i);
            Class<?> into = path.get(i + 1);

            if (intermediate.isEmpty()) {
                return Optional.empty();
            }

            intermediate = adjacencyList.get(from).get(into).apply(intermediate.get());
        }

        return (Optional<T>) intermediate;
    }

    private record Route(Class<?> source, Class<?> target) {}
}
