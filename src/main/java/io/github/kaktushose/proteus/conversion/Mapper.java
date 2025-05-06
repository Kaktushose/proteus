package io.github.kaktushose.proteus.conversion;

import org.jetbrains.annotations.NotNull;

public sealed interface Mapper {

    non-sealed interface UniMapper<S, T> extends Mapper {
        @NotNull
        Result<T> from(@NotNull S source, @NotNull MappingContext context);
    }

    non-sealed interface BiMapper<S, T> extends Mapper {
        @NotNull
        Result<T> from(@NotNull S source, @NotNull MappingContext context);

        @NotNull
        Result<S> into(@NotNull T target, @NotNull MappingContext context);
    }

    record MappingContext() {}

}
