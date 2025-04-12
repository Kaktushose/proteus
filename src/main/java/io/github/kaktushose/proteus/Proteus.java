package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.adapter.ReversibleTypAdapter;
import io.github.kaktushose.proteus.adapter.TypeAdapter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Proteus {

    private final Map<Class<?>, Map<Class<?>, Function<Object, Optional<Object>>>> adjacencyList = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <S, T> Proteus register(Class<S> source, Class<T> target, TypeAdapter<S, T> adapter) {
        adjacencyList.computeIfAbsent(source, unused -> new HashMap<>()).put(target, (TypeAdapter<Object, Object>) adapter);

        if (adapter instanceof ReversibleTypAdapter<S,T> reversibleTypAdapter) {
            adjacencyList.computeIfAbsent(target, unused -> new HashMap<>()).put(
                    source,
                    value -> ((ReversibleTypAdapter<Object, Object>) reversibleTypAdapter).reverse(value)
            );
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <S, T> Proteus register(Class<S> source, Class<T> target, Function<S, Optional<T>> from, Function<T, Optional<S>> into) {
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

    @SuppressWarnings("unchecked")
    public <S, T> Optional<T> convert(S value, Class<T> target) {
        var path = findPath(value.getClass(), target);
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

    private List<Class<?>> findPath(Class<?> source, Class<?> target) {
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
}
