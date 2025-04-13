package io.github.kaktushose.proteus.conversion;

public sealed interface Mapper {

    record MappingContext() {}

    non-sealed interface UniMapper<S, T> extends Mapper {
        Result<T> from(S source, MappingContext context);
    }

    non-sealed interface BiMapper<S, T> extends Mapper {
        Result<T> from(S source, MappingContext context);

        Result<S> into(T target, MappingContext context);
    }

}
