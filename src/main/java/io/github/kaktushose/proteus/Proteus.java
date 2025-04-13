package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.adapter.ReversibleTypAdapter;
import io.github.kaktushose.proteus.adapter.TypeAdapter;
import io.github.kaktushose.proteus.exception.CyclingConversionException;
import io.github.kaktushose.proteus.util.ConcurrentLruCache;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Proteus {

    private static final ThreadLocal<List<Function<?, ?>>> alreadyCalled = ThreadLocal.withInitial(ArrayList::new);
    private static final Map<Class<?>, Class<?>> primitiveMapping = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class
    );

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
        source = (Class<S>) primitiveMapping.getOrDefault(source, source);
        target = (Class<T>) primitiveMapping.getOrDefault(target, target);
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
        source = (Class<S>) primitiveMapping.getOrDefault(source, source);
        target = (Class<T>) primitiveMapping.getOrDefault(target, target);
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
        target = (Class<T>) primitiveMapping.getOrDefault(target, target);
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

            var vertex = adjacencyList.get(from).get(into);
            var stack = alreadyCalled.get();
            if (stack.contains(vertex)) {
                throw new CyclingConversionException(from, into, vertex, stack);
            }
            stack.add(vertex);
            intermediate = vertex.apply(intermediate.get());
            stack.remove(vertex);
        }

        return (Optional<T>) intermediate;
    }

    private record Route(Class<?> source, Class<?> target) {}
}
