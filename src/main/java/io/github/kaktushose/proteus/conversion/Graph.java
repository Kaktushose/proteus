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
    private static final Map<Class<?>, List<Class<?>>> allowedRoutes = Map.of(
            Type.Universal.class, List.of(Type.Universal.class),
            Type.Class.class, List.of(Type.Class.class, Type.Specific.class),
            Type.Specific.class, List.of(Type.Specific.class, Type.Class.class),
            Type.Parameterized.class, List.of(Type.Parameterized.class)
    );
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
        if (!allowedRoutes.get(adapter.source().getClass()).contains(adapter.target().getClass())) {
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
    @SuppressWarnings("unchecked")
    public <S, T> Result<T> convert(@Nullable S value, @NotNull Type<S> source, @NotNull Type<T> target) {
        if (!allowedRoutes.get(source.getClass()).contains(target.getClass())) {
            return Result.failure("Cannot mix different types!");
        }

        var path = pathCache.get(new Route(source, target));
        if (path.isEmpty()) {
            return Result.failure("Found no path to convert from '%s' to '%s'!".formatted(source, target));
        }

        Result<Object> intermediate = Result.success(value);
        for (Edge edge : path) {
            if (intermediate instanceof Result.Success<?>(Object success)) {
                intermediate = applyEdge(edge, success);
            }
            if (intermediate instanceof Result.Failure<?> failure) {
                intermediate = Result.failure(formatError(path, failure, edge));
            }
        }
        return (Result<T>) intermediate;
    }

    private Result<Object> applyEdge(Edge edge, Object value) {
        return switch (edge) {
            case Edge.ResolvedEdge resolved -> applyMapper(resolved, value);
            case Edge.UnresolvedEdge unresolved ->
                    convert(value, unresolved.from().universal(), unresolved.into().universal());
        };
    }

    private Result<Object> applyMapper(Edge.ResolvedEdge edge, Object value) {
        var mapper = edge.mapper();
        var stack = callStack.get();
        if (stack.contains(mapper)) {
            throw new CyclingConversionException(edge.from(), edge.into(), mapper, stack);
        }
        stack.add(mapper);
        var result = mapper.from(value, new Mapper.MappingContext());
        stack.remove(mapper);
        return result;
    }

    private record Route(Type<?> source, Type<?> target) {}

    @SuppressWarnings("unchecked")
    private List<Edge> findPath(Route route) {
        var source = route.source;
        var target = route.target;

        if (source.equals(target)) {
            return List.of();
        }

        if (source instanceof Type.Specific<?> from && target instanceof Type.Specific<?> into && from.equalsIgnoreContainer(into)) {
            return List.of(new Edge.UnresolvedEdge((Type.Specific<Object>) from, (Type.Specific<Object>) into));
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

                Path newPath = path.addEdge(neighbour, mapper(path.head, neighbour));
                if (equals(neighbour, target)) {
                    return newPath.edges();
                }
                queue.offer(newPath);
            }
        }
        return List.of();
    }

    @NotNull
    private Set<Type<?>> neighbours(@NotNull Type<?> type) {
        if (type instanceof Type.Specific<?> specific) {
            var mappers = adjacencyList.get(type);
            var result = new HashSet<Type<?>>();
            if (mappers != null) {
                result.addAll(mappers.keySet());
            }
            result.addAll(adjacencyList.keySet().stream()
                    .filter(Type.Specific.class::isInstance)
                    .filter(it -> ((Type.Specific<?>) it).equalsIgnoreContainer(specific))
                    .collect(Collectors.toSet()));
            return result;
        }
        return adjacencyList.getOrDefault(type, Map.of()).keySet();
    }

    @Nullable
    public UniMapper<?, ?> mapper(@NotNull Type<?> source, @NotNull Type<?> target) {
        var mappers = adjacencyList.getOrDefault(source, Map.of());
        return mappers.get(target);
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
    private record Path(List<Edge> edges, Type<Object> head) {

        public Path(Type<?> head) {
            this(new ArrayList<>(), (Type<Object>) head);
        }

        public Path addEdge(@NotNull Type<?> intermediate, @Nullable UniMapper<?, ?> mapper) {
            if (mapper != null) {
                edges.add(new Edge.ResolvedEdge(head, (Type<Object>) intermediate, (UniMapper<Object, Object>) mapper));
                return new Path(new ArrayList<>(edges), (Type<Object>) intermediate);
            }

            if (head instanceof Type.Specific<Object> from && intermediate instanceof Type.Specific<?> into) {
                if (!from.equalsIgnoreContainer(into)) {
                    throw new IllegalArgumentException("Illegal edge for two specific types with different format. Please report this error to the devs of proteus!");
                }
            } else {
                throw new IllegalArgumentException("Mapper cannot be null for non-specific types. Please report this error to the devs of proteus!");
            }

            edges.add(new Edge.UnresolvedEdge(from, (Type.Specific<Object>) into));
            return new Path(new ArrayList<>(edges), (Type<Object>) intermediate);
        }

        public List<Edge> edges() {
            return Collections.unmodifiableList(edges);
        }
    }

    private sealed interface Edge {

        Type<Object> from();

        Type<Object> into();

        record UnresolvedEdge(Type.Specific<Object> from, Type.Specific<Object> into) implements Edge {}

        record ResolvedEdge(Type<Object> from, Type<Object> into, UniMapper<Object, Object> mapper) implements Edge {}
    }

    private String formatError(List<Edge> path, Result.Failure<?> failure, @Nullable Edge step) {
        Type<?> from;
        Type<?> into;
        step = path.get(path.indexOf(step) - 1);
        if (step == null) {
            from = path.getLast().from();
            into = path.getLast().into();
        } else {
            from = step.from();
            into = step.into();
        }
        StringBuilder error = new StringBuilder();
        error.append("Failed to convert from '%s' to '%s'.\n".formatted(path.getFirst().from(), path.getLast().into()))
                .append("Type adapting failed for step '%s' -> '%s'! Reason: '%s'\n".formatted(from, into, failure.message()))
                .append("Path: \n");

        error.append("\n");
        for (Edge edge : path) {
            if (edge.from().equals(from) || edge.from().equals(into)) {
                error.append("  -> %s".formatted(edge.from()));
            } else {
                error.append("     ").append(edge.from());
            }
            error.append("\n");
        }
        var last = path.getLast();
        if (last != null) {
            if (last.into().equals(from) || last.into().equals(into)) {
                error.append("  -> %s".formatted(last.into()));
            } else {
                error.append("     ").append(last.into());
            }
        }
        return error.toString();
    }
}
