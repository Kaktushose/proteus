package io.github.kaktushose.proteus.conversion;

import io.github.kaktushose.proteus.graph.Edge;
import io.github.kaktushose.proteus.mapping.Mapper.UniMapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/// Thrown to indicate that a cycling [UniMapper] call occurred during conversion.
public class CyclingConversionException extends RuntimeException {

    /// Constructs a new CyclingConversionException.
    ///
    /// @param edge          the [Edge] that was called cyclic
    /// @param alreadyCalled the callstack of previously called [Edge]s
    public CyclingConversionException(@NotNull Edge edge, @NotNull List<Edge> alreadyCalled) {
        super("Cannot convert from '%s' to '%s' because of cycling source adapter calls!\n   -> %s\n      was called by %s".formatted(
                edge.from(),
                edge.into(),
                mapper(edge),
                reverse(alreadyCalled).stream()
                        .map(CyclingConversionException::mapper)
                        .collect(Collectors.joining("\n      was called by "))
        ));
    }

    @NotNull
    private static <T> List<T> reverse(@NotNull List<T> list) {
        Collections.reverse(list);
        return list;
    }

    @NotNull
    private static String mapper(@NotNull Edge edge) {
        UniMapper<Object, Object> mapper = edge.mapper();
        if (mapper.getClass().isSynthetic()) {
            return "Inlined Mapper[%s => %s]".formatted(edge.from(), edge.into());
        }
        return mapper.getClass().getName();
    }
}
