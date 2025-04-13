package io.github.kaktushose.proteus.conversion;

import io.github.kaktushose.proteus.conversion.type.Type;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CyclingConversionException extends RuntimeException {

    public CyclingConversionException(Type<?> from, Type<?> into, Mapper.UniMapper<Object, Object> vertex, List<Mapper.UniMapper<Object, Object>> alreadyCalled) {
        super("Cannot convert from '%s' to '%s' because of cycling type adapter calls!\n   -> %s\n      was called by %s".formatted(
                from,
                into,
                vertex,
                reverse(alreadyCalled).stream().map(it -> it.getClass().getName()).collect(Collectors.joining("\n      was called by "))
        ));
    }

    private static <T> List<T> reverse(List<T> list) {
        Collections.reverse(list);
        return list;
    }

}
