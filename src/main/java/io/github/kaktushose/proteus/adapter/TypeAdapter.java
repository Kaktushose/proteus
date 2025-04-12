package io.github.kaktushose.proteus.adapter;

import java.util.Optional;
import java.util.function.Function;

public interface TypeAdapter<S, T> extends Function<S, Optional<T>> {

    @Override
    Optional<T> apply(S source);

}
