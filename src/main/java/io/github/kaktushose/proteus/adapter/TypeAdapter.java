package io.github.kaktushose.proteus.adapter;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public interface TypeAdapter<S, T> extends Function<S, Optional<T>> {

    @NotNull
    @Override
    Optional<T> apply(@NotNull S source);

}
