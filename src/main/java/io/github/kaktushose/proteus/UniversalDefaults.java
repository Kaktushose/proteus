package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.Mapper;
import io.github.kaktushose.proteus.conversion.Result;
import io.github.kaktushose.proteus.graph.Graph;
import io.github.kaktushose.proteus.type.Type.Universal;
import io.github.kaktushose.proteus.type.TypeAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

import static io.github.kaktushose.proteus.conversion.Result.success;

final class UniversalDefaults {

    private static final Universal<Byte> BYTE = new Universal<>(Byte.class);
    private static final Universal<Short> SHORT = new Universal<>(Short.class);
    private static final Universal<Integer> INTEGER = new Universal<>(Integer.class);
    private static final Universal<Long> LONG = new Universal<>(Long.class);
    private static final Universal<Float> FLOAT = new Universal<>(Float.class);
    private static final Universal<Double> DOUBLE = new Universal<>(Double.class);
    private static final Universal<Character> CHARACTER = new Universal<>(Character.class);
    private static final Universal<String> STRING = new Universal<>(String.class);
    private static final Universal<StringBuilder> STRING_BUILDER = new Universal<>(StringBuilder.class);
    private static final Universal<StringBuffer> STRING_BUFFER = new Universal<>(StringBuffer.class);
    private static final Universal<Character[]> CHARACTER_ARRAY = new Universal<>(Character[].class);

    public static void registerMappers(Graph graph) {
        // byte
        graph.register(new TypeAdapter<>(BYTE, SHORT, ((source, context) -> success((short) source))));
        graph.register(new TypeAdapter<>(BYTE, INTEGER, ((source, context) -> success((int) source))));
        graph.register(new TypeAdapter<>(BYTE, LONG, ((source, context) -> success((long) source))));
        graph.register(new TypeAdapter<>(BYTE, FLOAT, ((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(BYTE, DOUBLE, ((source, context) -> success((double) source))));

        // short
        graph.register(new TypeAdapter<>(SHORT, INTEGER, ((source, context) -> success((int) source))));
        graph.register(new TypeAdapter<>(SHORT, LONG, ((source, context) -> success((long) source))));
        graph.register(new TypeAdapter<>(SHORT, FLOAT, ((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(SHORT, DOUBLE, ((source, context) -> success((double) source))));

        // char
        graph.register(new TypeAdapter<>(CHARACTER, INTEGER, ((source, context) -> success((int) source))));
        graph.register(new TypeAdapter<>(CHARACTER, LONG, ((source, context) -> success((long) source))));
        graph.register(new TypeAdapter<>(CHARACTER, FLOAT, ((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(CHARACTER, DOUBLE, ((source, context) -> success((double) source))));

        // int
        graph.register(new TypeAdapter<>(INTEGER, LONG, ((source, context) -> success((long) source))));
        graph.register(new TypeAdapter<>(INTEGER, FLOAT, ((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(INTEGER, DOUBLE, ((source, context) -> success((double) source))));

        // long
        graph.register(new TypeAdapter<>(LONG, FLOAT, ((source, context) -> success((float) source))));
        graph.register(new TypeAdapter<>(LONG, DOUBLE, ((source, context) -> success((double) source))));

        // float
        graph.register(new TypeAdapter<>(FLOAT, DOUBLE, ((source, context) -> success((double) source))));

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
