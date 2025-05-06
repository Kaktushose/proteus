package io.github.kaktushose.proteus.conversion;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public sealed interface Mapper {

    non-sealed interface UniMapper<S, T> extends Mapper {
        @NotNull
        Result<T> from(@NotNull S source, @NotNull MappingContext context);

        boolean lossless();

        static <S,T> UniMapper<S, T> lossy(@NotNull BiFunction<S, MappingContext, Result<T>> mapper) {
            return new UniMapper<>() {
                @Override
                public @NotNull Result<T> from(@NotNull S source, @NotNull MappingContext context) {
                    return mapper.apply(source, context);
                }

                @Override
                public boolean lossless() {
                    return false;
                }
            };
        }

        static <S,T> UniMapper<S, T> lossless(@NotNull BiFunction<S, MappingContext, Result<T>> mapper) {
            return new UniMapper<>() {
                @Override
                public @NotNull Result<T> from(@NotNull S source, @NotNull MappingContext context) {
                    return mapper.apply(source, context);
                }

                @Override
                public boolean lossless() {
                    return true;
                }
            };
        }

    }

    non-sealed interface BiMapper<S, T> extends Mapper {
        @NotNull
        Result<T> from(@NotNull S source, @NotNull MappingContext context);

        @NotNull
        Result<S> into(@NotNull T target, @NotNull MappingContext context);
    }

    record MappingContext() {}

}
