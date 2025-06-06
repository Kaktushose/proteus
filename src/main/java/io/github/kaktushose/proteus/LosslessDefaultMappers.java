package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.ApiStatus;

import java.math.BigDecimal;

import static io.github.kaktushose.proteus.mapping.Mapper.bi;
import static io.github.kaktushose.proteus.mapping.Mapper.uni;
import static io.github.kaktushose.proteus.mapping.MappingResult.failure;
import static io.github.kaktushose.proteus.mapping.MappingResult.lossless;

/// Default lossless mappers for primitive types following the widening and narrowing primitive conversion. Additionally,
///  provides bidirectional mappers for `char[]`, [String], [StringBuffer] and [StringBuilder] as well as a [Double] to
/// [BigDecimal] mapper.
///
/// @see <a href="https://docs.oracle.com/javase/specs/jls/se10/html/jls-5.html#jls-5.1.2">Java Language Specification</a>
@ApiStatus.Internal
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
    private static final Type<BigDecimal> BIG_DECIMAL = Type.of(BigDecimal.class);

    static void wideningPrimitives(Proteus proteus) {
        // byte
        proteus.register(BYTE, SHORT, uni((source, _) -> lossless((short) source)));
        proteus.register(BYTE, INTEGER, uni((source, _) -> lossless((int) source)));
        proteus.register(BYTE, LONG, uni((source, _) -> lossless((long) source)));
        proteus.register(BYTE, FLOAT, uni((source, _) -> lossless((float) source)));
        proteus.register(BYTE, DOUBLE, uni((source, _) -> lossless((double) source)));

        // short
        proteus.register(SHORT, INTEGER, uni((source, _) -> lossless((int) source)));
        proteus.register(SHORT, LONG, uni((source, _) -> lossless((long) source)));
        proteus.register(SHORT, FLOAT, uni((source, _) -> lossless((float) source)));
        proteus.register(SHORT, DOUBLE, uni((source, _) -> lossless((double) source)));

        // char
        proteus.register(CHARACTER, INTEGER, uni((source, _) -> lossless((int) source)));
        proteus.register(CHARACTER, LONG, uni((source, _) -> lossless((long) source)));
        proteus.register(CHARACTER, FLOAT, uni((source, _) -> lossless((float) source)));
        proteus.register(CHARACTER, DOUBLE, uni((source, _) -> lossless((double) source)));

        // int
        proteus.register(INTEGER, LONG, uni((source, _) -> lossless((long) source)));
        proteus.register(INTEGER, FLOAT, uni((source, _) -> lossless((float) source)));
        proteus.register(INTEGER, DOUBLE, uni((source, _) -> lossless((double) source)));

        // long
        proteus.register(LONG, FLOAT, uni((source, _) -> lossless((float) source)));
        proteus.register(LONG, DOUBLE, uni((source, _) -> lossless((double) source)));

        // float
        proteus.register(FLOAT, DOUBLE, uni((source, _) -> lossless((double) source)));
    }

    static void narrowingPrimitives(Proteus proteus) {
        proteus.register(DOUBLE, BYTE, uni((source, _) -> {
            if (source < Byte.MIN_VALUE || source > Byte.MAX_VALUE) {
                return failure("Number out of range for byte");
            }
            return lossless(source.byteValue());
        }));
        proteus.register(DOUBLE, SHORT, uni((source, _) -> {
            if (source < Short.MIN_VALUE || source > Short.MAX_VALUE) {
                return failure("Number out of range for short");
            }
            return lossless(source.shortValue());
        }));
        proteus.register(DOUBLE, CHARACTER, uni((source, _) -> {
            if (source < Character.MIN_VALUE || source > Character.MAX_VALUE) {
                return failure("Number out of range for char");
            }
            return lossless((char) source.doubleValue());
        }));
        proteus.register(DOUBLE, INTEGER, uni((source, _) -> {
            if (source < Integer.MIN_VALUE || source > Integer.MAX_VALUE) {
                return failure("Number out of range for integer");
            }
            return lossless(source.intValue());
        }));
        proteus.register(DOUBLE, LONG, uni((source, _) -> {
            if (source < Long.MIN_VALUE || source > Long.MAX_VALUE) {
                return failure("Number out of range for long");
            }
            return lossless(source.longValue());
        }));
        proteus.register(DOUBLE, FLOAT, uni((source, _) -> {
            if (source < Float.MIN_VALUE || source > Float.MAX_VALUE) {
                return failure("Number out of range for float");
            }
            return lossless(source.floatValue());
        }));
    }

    static void string(Proteus proteus) {
        // char array
        proteus.register(STRING, CHARACTER_ARRAY, bi(
                (source, _) -> lossless(source.toCharArray()),
                (target, _) -> lossless(new String(target))
        ));

        // string buffer
        proteus.register(STRING, STRING_BUFFER, bi(
                (source, _) -> lossless(new StringBuffer(source)),
                (target, _) -> lossless(target.toString())
        ));

        // string builder
        proteus.register(STRING, STRING_BUILDER, bi(
                (source, _) -> lossless(new StringBuilder(source)),
                (target, _) -> lossless(target.toString())
        ));
    }

    static void bigDecimal(Proteus proteus) {
        proteus.register(DOUBLE, BIG_DECIMAL, uni((source, _) -> lossless(new BigDecimal(source))));
    }
}
