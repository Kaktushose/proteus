package io.github.kaktushose.proteus.type.internal;

import io.github.kaktushose.proteus.type.TypeReference;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Java<T>(@NotNull TypeReference<T> reference) implements Type<T> {

    public Java {
        Objects.requireNonNull(reference);
    }
}
