package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.CyclingConversionException;
import io.github.kaktushose.proteus.conversion.Mapper;
import io.github.kaktushose.proteus.conversion.Result;
import io.github.kaktushose.proteus.graph.Edge;
import io.github.kaktushose.proteus.graph.Graph;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.util.Helpers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Proteus {

    private static final ThreadLocal<List<Mapper.UniMapper<Object, Object>>> callStack = ThreadLocal.withInitial(ArrayList::new);
    private final Graph graph;

    public Proteus() {
        this(1000);
    }

    public Proteus(int cacheSize) {
        this.graph = new Graph(cacheSize);
        UniversalDefaults.registerMappers(graph);
    }

    @NotNull
    public Graph graph() {
        return graph;
    }

    @NotNull
    public <S, T> Result<T> convert(@NotNull S value, @NotNull Type<S> source, @NotNull Type<T> target) {
        return convert(value, source, target, false);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <S, T> Result<T> convert(@NotNull S value, @NotNull Type<S> source, @NotNull Type<T> target, boolean lossless) {
        if (Helpers.invalidRoute(source.getClass(), target.getClass())) {
            return Result.failure("Cannot mix different types!");
        }

        var path = graph.path(source, target);
        if (path.isEmpty()) {
            return Result.failure("Found no path to convert from '%s' to '%s'!".formatted(source, target));
        }

        Result<Object> intermediate = Result.success(value);
        for (Edge edge : path) {
            if (intermediate instanceof Result.Success<?>(Object success)) {
                intermediate = applyEdge(edge, success, lossless);
            }
            if (intermediate instanceof Result.Failure<?> failure) {
                intermediate = Result.failure(Helpers.formatError(path, failure, edge));
            }
        }
        return (Result<T>) intermediate;
    }

    @NotNull
    private Result<Object> applyEdge(@NotNull Edge edge, @NotNull Object value, boolean lossless) {
        return switch (edge) {
            case Edge.ResolvedEdge resolved -> applyMapper(resolved, value, lossless);
            case Edge.UnresolvedEdge unresolved ->
                    convert(value, unresolved.from().universal(), unresolved.into().universal());
        };
    }

    @NotNull
    private Result<Object> applyMapper(@NotNull Edge.ResolvedEdge edge, @NotNull Object value, boolean lossless) {
        var mapper = edge.mapper();
        var stack = callStack.get();
        if (stack.contains(mapper)) {
            throw new CyclingConversionException(edge.from(), edge.into(), mapper, stack);
        }
        if (lossless && !mapper.lossless()) {
            return Result.failure("No lossless conversion possible");
        }
        stack.add(mapper);
        var result = mapper.from(value, new Mapper.MappingContext());
        stack.remove(mapper);
        return result;
    }
}
