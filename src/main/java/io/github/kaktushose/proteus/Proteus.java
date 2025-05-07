package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.conversion.CyclingConversionException;
import io.github.kaktushose.proteus.graph.Edge;
import io.github.kaktushose.proteus.graph.Graph;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.Mapper.MappingContext;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.github.kaktushose.proteus.conversion.ConversionResult.Failure.ErrorType.*;

public class Proteus {

    private static final ThreadLocal<List<Mapper.UniMapper<Object, Object>>> callStack = ThreadLocal.withInitial(ArrayList::new);
    private final Graph graph;

    public Proteus() {
        this(1000);
    }

    public Proteus(int cacheSize) {
        this.graph = new Graph(cacheSize);
        LosslessDefaultMappers.registerMappers(graph);
    }

    @NotNull
    public Graph graph() {
        return graph;
    }

    @NotNull
    public <S, T> ConversionResult<T> convert(@NotNull S value, @NotNull Type<S> source, @NotNull Type<T> target) {
        return convert(value, source, target, false);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <S, T> ConversionResult<T> convert(@NotNull S value, @NotNull Type<S> source, @NotNull Type<T> target, boolean lossless) {
        List<Edge> path = graph.path(source, target);
        if (path.isEmpty()) {
            return new ConversionResult.Failure<>(NO_PATH_FOUND, "Found no path to convert from '%s' to '%s'!".formatted(source, target), null);
        }

        ConversionResult<Object> intermediate = new ConversionResult.Success<>(value);
        for (Edge edge : path) {
            if (intermediate instanceof ConversionResult.Success<?>(Object success)) {
                intermediate = applyEdge(edge, path, success, lossless);
            }
        }
        return (ConversionResult<T>) intermediate;
    }

    @NotNull
    private ConversionResult<Object> applyEdge(@NotNull Edge edge, @NotNull List<Edge> path, @NotNull Object value, boolean lossless) {
        return switch (edge) {
            case Edge.ResolvedEdge resolved -> applyMapper(resolved, path, value, lossless);
            case Edge.UnresolvedEdge(Type<Object> from, Type<Object> into) -> convert(value, Type.of(from.container()), Type.of(into.container()));
        };
    }

    @NotNull
    private ConversionResult<Object> applyMapper(@NotNull Edge.ResolvedEdge edge, @NotNull List<Edge> path, @NotNull Object value, boolean lossless) {
        Mapper.UniMapper<Object, Object> mapper = edge.mapper();
        List<Mapper.UniMapper<Object, Object>> stack = callStack.get();
        if (stack.contains(mapper)) {
            throw new CyclingConversionException(edge.from(), edge.into(), mapper, stack);
        }
        MappingContext context = new MappingContext(path, edge);
        if (lossless && !mapper.lossless()) {
            return new ConversionResult.Failure<>(NO_LOSSLESS_CONVERSION, "No lossless conversion possible", context);
        }
        stack.add(mapper);

        ConversionResult<Object> result = switch (mapper.from(value, context)) {
            case MappingResult.Success<Object>(Object success) -> new ConversionResult.Success<>(success);
            case MappingResult.Failure<Object>(String message) -> new ConversionResult.Failure<>(MAPPING_FAILED, message, context);
        };

        stack.remove(mapper);
        return result;
    }
}
