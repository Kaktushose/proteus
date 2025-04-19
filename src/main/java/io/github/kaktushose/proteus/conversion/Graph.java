package io.github.kaktushose.proteus.conversion;

import io.github.kaktushose.proteus.conversion.Mapper.BiMapper;
import io.github.kaktushose.proteus.conversion.Mapper.UniMapper;
import io.github.kaktushose.proteus.conversion.type.Type;
import io.github.kaktushose.proteus.conversion.type.TypeAdapter;
import io.github.kaktushose.proteus.util.ConcurrentLruCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Graph {

    private static final ThreadLocal<List<UniMapper<Object, Object>>> callStack = ThreadLocal.withInitial(ArrayList::new);
    private final Map<Type<?>, Map<Type<?>, UniMapper<Object, Object>>> adjacencyList;
    private final ConcurrentLruCache<Route, List<Edge>> pathCache;

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
        if (!adapter.source().getClass().equals(adapter.target().getClass())) {
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

    private Set<Type<?>> neighbours(Type<?> type) {
        if (type instanceof Type.Specific<?> specific) {
            var mapper = adjacencyList.get(type);
            if (mapper != null) {
                return mapper.keySet();
            }
            return adjacencyList.keySet().stream()
                    .filter(Type.Specific.class::isInstance)
                    .filter(it -> ((Type.Specific<?>) it).equalsIgnoreContainer(specific))
                    .collect(Collectors.toSet());
        }
        return adjacencyList.getOrDefault(type, Map.of()).keySet();
    }

    public UniMapper<?, ?> mapper(Type<?> source, Type<?> target) {
        var mappers = adjacencyList.get(source);
        if (mappers != null) {
            return mappers.get(target);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <S, T> Result<T> convert(S value, Type<S> source, Type<T> target) {
        if (source instanceof Type.Universal<?> ^ target instanceof Type.Universal<?>) {
            return Result.failure("Cannot convert between universal and non-universal type!");
        }

        var path = pathCache.get(new Route(source, target));
        if (path.isEmpty()) {
            return Result.failure("Found no path to convert from '%s' to '%s'!".formatted(source, target));
        }

        Result<Object> intermediate = Result.success(value);
        for (Edge step : path) {
            switch (intermediate) {
                case Result.Success<?>(Object success) -> {
                    var mapper = step.mapper();
                    var callStack = Graph.callStack.get();

                    if (callStack.contains(mapper)) {
                        throw new CyclingConversionException(step.from(), step.into(), mapper, callStack);
                    }

                    // a null mapper indicates an implicit specific conversion, we have to find a route for the container types
                    // for any other case the mapper cannot be null, see the Edge compact constructor
                    if (mapper == null) {
                        intermediate = convert(success, ((Type.Specific<Object>) step.from).universal(), ((Type.Specific<Object>) step.into).universal());
                        break;
                    }

                    callStack.add(mapper);
                    intermediate = mapper.from(success, new Mapper.MappingContext());
                    callStack.remove(mapper);
                }
                case Result.Failure<?> failure -> {
                    return Result.failure(formatError(path, failure, step));
                }
            }
        }

        if (intermediate instanceof Result.Failure<?> failure) {
            intermediate = Result.failure(formatError(path, failure, null));
        }

        return (Result<T>) intermediate;
    }

    @SuppressWarnings("unchecked")
    private List<Edge> findPath(Route route) {
        var source = route.source;
        var target = route.target;

        if (source.equals(target)) {
            return List.of();
        }

        if (source instanceof Type.Specific<?> specificSource &&
            target instanceof Type.Specific<?> specificTarget &&
            specificSource.equalsIgnoreContainer(specificTarget)) {
            return List.of(new Edge((Type<Object>) source, (Type<Object>) target, null));
        }

        Queue<Path> queue = new LinkedList<>();
        Set<Type<?>> visited = new HashSet<>();
        queue.offer(new Path(source));
        visited.add(source);
        while (!queue.isEmpty()) {
            Path path = queue.poll();
            for (Type<?> neighbour : neighbours(path.head)) {
                if (visited.contains(neighbour)) {
                    continue;
                }
                visited.add(neighbour);

                Path newPath = path.add(neighbour, mapper(path.head, neighbour));
                if (equals(neighbour, target)) {
                    return newPath.steps();
                }
                queue.offer(newPath);
            }
        }
        return List.of();
    }

    private boolean equals(Type<?> first, Type<?> second) {
        if (first.equals(second)) {
            return true;
        }
        if (first instanceof Type.Specific<?> specificFirst && second instanceof Type.Specific<?> specificSecond) {
            return specificFirst.equalsIgnoreContainer(specificSecond);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private record Path(List<Edge> steps, Type<Object> head) {

        public Path(Type<?> head) {
            this(new ArrayList<>(), (Type<Object>) head);
        }

        public Path add(Type<?> intermediate, UniMapper<?, ?> mapper) {
            steps.add(new Edge(head, (Type<Object>) intermediate, (UniMapper<Object, Object>) mapper));
            return new Path(new ArrayList<>(steps), (Type<Object>) intermediate);
        }

        public List<Edge> steps() {
            return Collections.unmodifiableList(steps);
        }
    }

    private record Edge(Type<Object> from, Type<Object> into, @Nullable UniMapper<Object, Object> mapper) {

        public Edge {
            if (mapper == null) {
                if (from instanceof Type.Specific<Object> specificSource &&
                    into instanceof Type.Specific<Object> specificTarget) {
                    if (!specificSource.equalsIgnoreContainer(specificTarget)) {
                        throw new IllegalArgumentException("Illegal edge for two specific types with different format. Please report this error to the devs of proteus!");
                    }
                } else {
                    throw new IllegalArgumentException("Mapper cannot be null for non-specific types. Please report this error to the devs of proteus!");
                }
            }
        }
    }

    private record Route(Type<?> source, Type<?> target) {}

    private String formatError(List<Edge> path, Result.Failure<?> failure, @Nullable Edge step) {
        Type<?> from;
        Type<?> into;
        step = path.get(path.indexOf(step) - 1);
        if (step == null) {
            from = path.getLast().from();
            into = path.getLast().into();
        } else {
            from = step.from;
            into = step.into;
        }
        StringBuilder error = new StringBuilder();
        error.append("Failed to convert from '%s' to '%s'.\n".formatted(path.getFirst().from, path.getLast().into))
                .append("Type adapting failed for step '%s' -> '%s'! Reason: '%s'\n".formatted(from, into, failure.message()))
                .append("Path: \n");

        error.append("\n");
        for (Edge edge : path) {
            if (edge.from.equals(from) || edge.from.equals(into)) {
                error.append("  -> %s".formatted(edge.from));
            } else {
                error.append("     ").append(edge.from);
            }
            error.append("\n");
        }
        var last = path.getLast();
        if (last != null) {
            if (last.into.equals(from) || last.into.equals(into)) {
                error.append("  -> %s".formatted(last.into));
            } else {
                error.append("     ").append(last.into);
            }
        }
        return error.toString();
    }


}
