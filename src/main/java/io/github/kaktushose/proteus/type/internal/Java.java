package io.github.kaktushose.proteus.type.internal;

import io.github.kaktushose.proteus.type.ParameterizedTypeReference;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Java<T>(@NotNull ParameterizedTypeReference<T> reference) implements Type<T> {

    public Java {
        Objects.requireNonNull(reference);
    }
}
