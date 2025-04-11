package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.adapter.TypeAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Proteus {

    private final Map<Class<?>, Map<Class<?>, TypeAdapter<?, ?>>> adjacencyList = new HashMap<>();

    public <S, T> void register(Class<S> source, Class<T> target, TypeAdapter<S, T> adapter) {
        adjacencyList.computeIfAbsent(source, unused -> new HashMap<>()).put(target, adapter);
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
            TypeAdapter<?, ?> adapter = adjacencyList.get(from).get(into);
            try {
                if (intermediate.isEmpty()) {
                    return Optional.empty();
                }
                intermediate = (Optional<Object>) adapter.getClass().getDeclaredMethod("adapt", from).invoke(adapter, intermediate.get());
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
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
