package io.github.kaktushose.proteus.conversion;

import io.github.kaktushose.proteus.conversion.Mapper.BiMapper;
import io.github.kaktushose.proteus.conversion.Mapper.UniMapper;
import io.github.kaktushose.proteus.conversion.type.Type;
import io.github.kaktushose.proteus.conversion.type.TypeAdapter;
import io.github.kaktushose.proteus.util.ConcurrentLruCache;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Graph {

    private static final ThreadLocal<List<UniMapper<Object, Object>>> callStack = ThreadLocal.withInitial(ArrayList::new);
    private final Map<Type<?>, Map<Type<?>, List<UniMapper<Object, Object>>>> adjacencyList;
    private final ConcurrentLruCache<Route, List<Type<?>>> pathCache;

    public Graph() {
        this(1000);
    }

    public Graph(int cacheSize) {
        pathCache = new ConcurrentLruCache<>(cacheSize, this::findPath);
        adjacencyList = new ConcurrentHashMap<>();
    }

    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Graph register(@NotNull TypeAdapter<?, ?> adapter) {
        switch ((Mapper) adapter.mapper()) {
            case UniMapper uniMapper -> {
                add(adapter.source(), adapter.target(), uniMapper);
            }
            case BiMapper biMapper -> {
                add(adapter.source(), adapter.target(), biMapper::from);
                add(adapter.target(), adapter.source(), biMapper::into);
            }
        }
        return this;
    }

    private void add(@NotNull Type<?> source, @NotNull Type<?> target, @NotNull UniMapper<Object, Object> adapter) {
        adjacencyList.computeIfAbsent(source, unused -> new HashMap<>())
                .computeIfAbsent(target, unused -> new ArrayList<>())
                .add(adapter);
    }

    @SuppressWarnings("unchecked")
    public <S, T> Result<T> convert(S value, Type<S> source, Type<T> target) {
        var path = pathCache.get(new Route(source, target));
        if (path.isEmpty()) {
            return Result.failure("Found no path to convert from '%s' to '%s'".formatted(source, target));
        }

        Result<Object> intermediate = Result.success(value);
        for (int i = 0; i < path.size() - 1; i++) {
            Type<?> from = path.get(i);
            Type<?> into = path.get(i + 1);

            switch (intermediate) {
                case Result.Success<?> success -> {
                    var vertex = adjacencyList.get(from).get(into).getLast();
                    var stack = callStack.get();
                    if (stack.contains(vertex)) {
                        throw new CyclingConversionException(from, into, vertex, stack);
                    }
                    stack.add(vertex);
                    intermediate = vertex.from(success.value(), new Mapper.MappingContext());
                    stack.remove(vertex);
                }
                case Result.Failure<?> failure -> {
                    // i - 1 because we check in the next step
                    return Result.failure(formatError(path, i - 1, failure));
                }
            }
        }
        if (intermediate instanceof Result.Failure<?> failure) {
            intermediate = Result.failure(formatError(path, path.size() - 2, failure));
        }
        return (Result<T>) intermediate;
    }

    private String formatError(List<Type<?>> path, int index, Result.Failure<?> failure) {
        Type<?> from = path.get(index);
        Type<?> into = path.get(index + 1);
        StringBuilder error = new StringBuilder();
        error.append("Failed to convert from '%s' to '%s'.\n".formatted(path.getFirst(), path.getLast()))
                .append("Type adapting failed for step '%s' -> '%s'! Reason: '%s'\n".formatted(from, into, failure.message()))
                .append("Path: \n");
        for (int i = 0; i < path.size(); i++) {
            if (i == index || i == index + 1) {
                error.append("  -> %s".formatted(path.get(i)));
            } else {
                error.append("     ").append(path.get(i));
            }
            error.append("\n");
        }
        return error.toString();
    }

    private List<Type<?>> findPath(Route route) {
        var source = route.source;
        var target = route.target;
        if (source.equals(target)) {
            return List.of(source);
        }

        Queue<List<Type<?>>> queue = new LinkedList<>();
        Set<Type<?>> visited = new HashSet<>();
        queue.offer(List.of(source));
        visited.add(source);

        while (!queue.isEmpty()) {
            List<Type<?>> path = queue.poll();
            Type<?> node = path.getLast();

            for (Type<?> neighbour : adjacencyList.getOrDefault(node, Map.of()).keySet()) {
                if (visited.contains(neighbour)) {
                    continue;
                }
                visited.add(neighbour);

                List<Type<?>> newPath = new ArrayList<>(path);
                newPath.add(neighbour);
                if (neighbour.equals(target)) {
                    return Collections.unmodifiableList(newPath);
                }
                queue.offer(newPath);
            }
        }
        return List.of();
    }

    private record Route(Type<?> source, Type<?> target) {}

}
