package io.github.kaktushose.proteus.adapter;

import java.util.Optional;

public interface ReversibleTypAdapter<S, T> extends TypeAdapter<S, T> {

    Optional<S> reverse(T target);

}
