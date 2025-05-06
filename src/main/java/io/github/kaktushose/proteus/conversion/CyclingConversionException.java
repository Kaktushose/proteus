package io.github.kaktushose.proteus.conversion;

import io.github.kaktushose.proteus.conversion.Mapper.UniMapper;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CyclingConversionException extends RuntimeException {

    public CyclingConversionException(@Nullable Type<?> from,
                                      @Nullable Type<?> into,
                                      @Nullable UniMapper<Object, Object> mapper,
                                      @NotNull List<UniMapper<Object, Object>> alreadyCalled) {
        super("Cannot convert from '%s' to '%s' because of cycling type adapter calls!\n   -> %s\n      was called by %s".formatted(
                from,
                into,
                mapper,
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
