package io.github.kaktushose.proteus.type.internal;

import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Universal<T>(@NotNull Class<T> clazz) implements Type<T> {

    public Universal {
        Objects.requireNonNull(clazz);
    }
}
