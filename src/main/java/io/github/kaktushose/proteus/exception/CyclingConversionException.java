package io.github.kaktushose.proteus.exception;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CyclingConversionException extends RuntimeException {

    public CyclingConversionException(Class<?> from, Class<?> into, Function<Object, Optional<Object>> vertex, List<Function<?, ?>> alreadyCalled) {
        super("Cannot convert from '%s' to '%s' because of cycling type adapter calls!\n   -> %s\n      was called by %s".formatted(
                from.getName(),
                into.getName(),
                vertex.getClass().getName(),
                reverse(alreadyCalled).stream().map(it -> it.getClass().getName()).collect(Collectors.joining("\n      was called by "))
        ));
    }

    private static <T> List<T> reverse(List<T> list) {
        Collections.reverse(list);
        return list;
    }

}
