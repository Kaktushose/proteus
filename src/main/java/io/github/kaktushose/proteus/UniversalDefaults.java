package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.Mapper;
import io.github.kaktushose.proteus.conversion.Result;
import io.github.kaktushose.proteus.graph.Graph;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

import static io.github.kaktushose.proteus.conversion.Mapper.UniMapper.lossless;
import static io.github.kaktushose.proteus.conversion.Result.success;

final class UniversalDefaults {

    private static final Type<Byte> BYTE = Type.universal(Byte.class);
    private static final Type<Short> SHORT = Type.universal(Short.class);
    private static final Type<Integer> INTEGER = Type.universal(Integer.class);
    private static final Type<Long> LONG = Type.universal(Long.class);
    private static final Type<Float> FLOAT = Type.universal(Float.class);
    private static final Type<Double> DOUBLE = Type.universal(Double.class);
    private static final Type<Character> CHARACTER = Type.universal(Character.class);
    private static final Type<String> STRING = Type.universal(String.class);
    private static final Type<StringBuilder> STRING_BUILDER = Type.universal(StringBuilder.class);
    private static final Type<StringBuffer> STRING_BUFFER = Type.universal(StringBuffer.class);
    private static final Type<Character[]> CHARACTER_ARRAY = Type.universal(Character[].class);

    public static void registerMappers(Graph graph) {
        // byte
        graph.register(new TypeAdapter<>(BYTE, SHORT, lossless((source, context) -> success((short) source))));
        graph.register(new TypeAdapter<>(BYTE, INTEGER, lossless((source, context) -> success((int) source))));
        graph.register(new TypeAdapter<>(BYTE, LONG, lossless((source, context) -> success((long) source))));
        graph.register(new TypeAdapter<>(BYTE, FLOAT, lossless((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(BYTE, DOUBLE, lossless((source, context) -> success((double) source))));

        // short
        graph.register(new TypeAdapter<>(SHORT, INTEGER, lossless((source, context) -> success((int) source))));
        graph.register(new TypeAdapter<>(SHORT, LONG, lossless((source, context) -> success((long) source))));
        graph.register(new TypeAdapter<>(SHORT, FLOAT, lossless((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(SHORT, DOUBLE, lossless((source, context) -> success((double) source))));

        // char
        graph.register(new TypeAdapter<>(CHARACTER, INTEGER, lossless((source, context) -> success((int) source))));
        graph.register(new TypeAdapter<>(CHARACTER, LONG, lossless((source, context) -> success((long) source))));
        graph.register(new TypeAdapter<>(CHARACTER, FLOAT, lossless((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(CHARACTER, DOUBLE, lossless((source, context) -> success((double) source))));

        // int
        graph.register(new TypeAdapter<>(INTEGER, LONG, lossless((source, context) -> success((long) source))));
        graph.register(new TypeAdapter<>(INTEGER, FLOAT, lossless((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(INTEGER, DOUBLE, lossless((source, context) -> success((double) source))));

        // long
        graph.register(new TypeAdapter<>(LONG, FLOAT, lossless((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(LONG, DOUBLE, lossless((source, context) -> success((double) source))));

        // float
        graph.register(new TypeAdapter<>(FLOAT, DOUBLE, lossless((source, context) -> success((double) source))));

        // char array
        graph.register(new TypeAdapter<>(STRING, CHARACTER_ARRAY, new Mapper.BiMapper<>() {
            @NotNull
            @Override
            public Result<Character[]> from(@NotNull String source, @NotNull MappingContext context) {
                return success(source.chars().mapToObj(c -> (char) c).toArray(Character[]::new));
            }

            @NotNull
            @Override
            public Result<String> into(@NotNull Character @NotNull [] target, @NotNull MappingContext context) {
                return success(Arrays.stream(target).map(String::valueOf).collect(Collectors.joining()));
            }
        }));

        // string buffer
        graph.register(new TypeAdapter<>(STRING, STRING_BUFFER, new Mapper.BiMapper<>() {
            @NotNull
            @Override
            public Result<StringBuffer> from(@NotNull String source, @NotNull MappingContext context) {
                return success(new StringBuffer(source));
            }

            @NotNull
            @Override
            public Result<String> into(@NotNull StringBuffer target, @NotNull MappingContext context) {
                return success(target.toString());
            }
        }));

        // string builder
        graph.register(new TypeAdapter<>(STRING, STRING_BUILDER, new Mapper.BiMapper<>() {
            @NotNull
            @Override
            public Result<StringBuilder> from(@NotNull String source, @NotNull MappingContext context) {
                return success(new StringBuilder(source));
            }

            @NotNull
            @Override
            public Result<String> into(@NotNull StringBuilder target, @NotNull MappingContext context) {
                return success(target.toString());
            }
        }));
    }
}
