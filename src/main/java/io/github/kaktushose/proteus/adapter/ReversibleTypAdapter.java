package io.github.kaktushose.proteus.adapter;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ReversibleTypAdapter<S, T> extends TypeAdapter<S, T> {

    @NotNull
    Optional<S> reverse(@NotNull T target);

}
