package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.graph.Graph;
import io.github.kaktushose.proteus.type.Type;

import java.util.Arrays;
import java.util.stream.Collectors;

import static io.github.kaktushose.proteus.mapping.Mapper.lossless;
import static io.github.kaktushose.proteus.mapping.MappingResult.success;

final class LosslessDefaultMappers {

    private static final Type<Byte> BYTE = Type.of(Byte.class);
    private static final Type<Short> SHORT = Type.of(Short.class);
    private static final Type<Integer> INTEGER = Type.of(Integer.class);
    private static final Type<Long> LONG = Type.of(Long.class);
    private static final Type<Float> FLOAT = Type.of(Float.class);
    private static final Type<Double> DOUBLE = Type.of(Double.class);
    private static final Type<Character> CHARACTER = Type.of(Character.class);
    private static final Type<String> STRING = Type.of(String.class);
    private static final Type<StringBuilder> STRING_BUILDER = Type.of(StringBuilder.class);
    private static final Type<StringBuffer> STRING_BUFFER = Type.of(StringBuffer.class);
    private static final Type<Character[]> CHARACTER_ARRAY = Type.of(Character[].class);

    public static void registerMappers(Proteus p) {
        // byte
        p.register(BYTE, SHORT, lossless((source, context) -> success((short) source)));
        p.register(BYTE, INTEGER, lossless((source, context) -> success((int) source)));
        p.register(BYTE, LONG, lossless((source, context) -> success((long) source)));
        p.register(BYTE, FLOAT, lossless((source, context) -> success((float) source)));
        p.register(BYTE, DOUBLE, lossless((source, context) -> success((double) source)));

        // short
        p.register(SHORT, INTEGER, lossless((source, context) -> success((int) source)));
        p.register(SHORT, LONG, lossless((source, context) -> success((long) source)));
        p.register(SHORT, FLOAT, lossless((source, context) -> success((float) source)));
        p.register(SHORT, DOUBLE, lossless((source, context) -> success((double) source)));

        // char
        p.register(CHARACTER, INTEGER, lossless((source, context) -> success((int) source)));
        p.register(CHARACTER, LONG, lossless((source, context) -> success((long) source)));
        p.register(CHARACTER, FLOAT, lossless((source, context) -> success((float) source)));
        p.register(CHARACTER, DOUBLE, lossless((source, context) -> success((double) source)));

        // int
        p.register(INTEGER, LONG, lossless((source, context) -> success((long) source)));
        p.register(INTEGER, FLOAT, lossless((source, context) -> success((float) source)));
        p.register(INTEGER, DOUBLE, lossless((source, context) -> success((double) source)));

        // long
        p.register(LONG, FLOAT, lossless((source, context) -> success((float) source)));
        p.register(LONG, DOUBLE, lossless((source, context) -> success((double) source)));

        // float
        p.register(FLOAT, DOUBLE, lossless((source, context) -> success((double) source)));

        // char array
        p.register(STRING, CHARACTER_ARRAY, lossless(
                (source, context) -> success(source.chars().mapToObj(c -> (char) c).toArray(Character[]::new)),
                (target, context) -> success(Arrays.stream(target).map(String::valueOf).collect(Collectors.joining()))
        ));

        // string buffer
        p.register(STRING, STRING_BUFFER, lossless(
                (source, context) -> success(new StringBuffer(source)),
                (target, context) -> success(target.toString())
        ));

        // string builder
        p.register(STRING, STRING_BUILDER, lossless(
                (source, context) -> success(new StringBuilder(source)),
                (target, context) -> success(target.toString())
        ));
    }
}
