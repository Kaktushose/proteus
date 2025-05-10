package io.github.kaktushose.proteus.conversion;

import io.github.kaktushose.proteus.mapping.Mapper.UniMapper;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/// Thrown to indicate that a cycling [UniMapper] call occurred during conversion.
public class CyclingConversionException extends RuntimeException {

    /// Constructs a new CyclingConversionException.
    ///
    /// @param from          the source [Type] of the path
    /// @param into          the destination [Type] of the path
    /// @param mapper        the [UniMapper] that was called cyclic
    /// @param alreadyCalled the callstack of previously called [UniMapper]s
    public CyclingConversionException(@Nullable Type<?> from,
                                      @Nullable Type<?> into,
                                      @Nullable UniMapper<Object, Object> mapper,
                                      @NotNull List<UniMapper<Object, Object>> alreadyCalled) {
        super("Cannot convert from '%s' to '%s' because of cycling source adapter calls!\n   -> %s\n      was called by %s".formatted(
                from,
                into,
                mapper != null ? mapper.getClass().getName() : null,
                reverse(alreadyCalled).stream()
                        .map(it -> it.getClass().getName())
                        .collect(Collectors.joining("\n      was called by "))
        ));
    }

    @NotNull
    private static <T> List<T> reverse(@NotNull List<T> list) {
        Collections.reverse(list);
        return list;
    }
}
