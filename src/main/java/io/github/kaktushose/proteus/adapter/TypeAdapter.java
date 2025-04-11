package io.github.kaktushose.proteus.adapter;

import java.util.Optional;

public interface TypeAdapter<S, T> {

    Optional<T> adapt(S source);

    default Optional<S> reverse(T target) {
         return Optional.empty();
    }

}
