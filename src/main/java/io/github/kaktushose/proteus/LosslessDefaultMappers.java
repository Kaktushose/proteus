package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.type.Type;

import static io.github.kaktushose.proteus.mapping.Mapper.lossless;
import static io.github.kaktushose.proteus.mapping.MappingResult.success;

/// Default lossless mappers for primitive types following the widening primitive conversion. Additionally, provides
/// bidirectional mappers for `char[]`, [String], [StringBuffer] and [StringBuilder].
///
/// @see <a href="https://docs.oracle.com/javase/specs/jls/se10/html/jls-5.html#jls-5.1.2">Java Language Specification</a>
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
    private static final Type<char[]> CHARACTER_ARRAY = Type.of(char[].class);

    static void registerMappers(Proteus proteus) {
        // byte
        proteus.register(BYTE, SHORT, lossless((source, context) -> success((short) source)));
        proteus.register(BYTE, INTEGER, lossless((source, context) -> success((int) source)));
        proteus.register(BYTE, LONG, lossless((source, context) -> success((long) source)));
        proteus.register(BYTE, FLOAT, lossless((source, context) -> success((float) source)));
        proteus.register(BYTE, DOUBLE, lossless((source, context) -> success((double) source)));

        // short
        proteus.register(SHORT, INTEGER, lossless((source, context) -> success((int) source)));
        proteus.register(SHORT, LONG, lossless((source, context) -> success((long) source)));
        proteus.register(SHORT, FLOAT, lossless((source, context) -> success((float) source)));
        proteus.register(SHORT, DOUBLE, lossless((source, context) -> success((double) source)));

        // char
        proteus.register(CHARACTER, INTEGER, lossless((source, context) -> success((int) source)));
        proteus.register(CHARACTER, LONG, lossless((source, context) -> success((long) source)));
        proteus.register(CHARACTER, FLOAT, lossless((source, context) -> success((float) source)));
        proteus.register(CHARACTER, DOUBLE, lossless((source, context) -> success((double) source)));

        // int
        proteus.register(INTEGER, LONG, lossless((source, context) -> success((long) source)));
        proteus.register(INTEGER, FLOAT, lossless((source, context) -> success((float) source)));
        proteus.register(INTEGER, DOUBLE, lossless((source, context) -> success((double) source)));

        // long
        proteus.register(LONG, FLOAT, lossless((source, context) -> success((float) source)));
        proteus.register(LONG, DOUBLE, lossless((source, context) -> success((double) source)));

        // float
        proteus.register(FLOAT, DOUBLE, lossless((source, context) -> success((double) source)));

        // char array
        proteus.register(STRING, CHARACTER_ARRAY, lossless(
                (source, context) -> success(source.toCharArray()),
                (target, context) -> success(new String(target))
        ));

        // string buffer
        proteus.register(STRING, STRING_BUFFER, lossless(
                (source, context) -> success(new StringBuffer(source)),
                (target, context) -> success(target.toString())
        ));

        // string builder
        proteus.register(STRING, STRING_BUILDER, lossless(
                (source, context) -> success(new StringBuilder(source)),
                (target, context) -> success(target.toString())
        ));
    }
}
