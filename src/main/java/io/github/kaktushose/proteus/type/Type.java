package io.github.kaktushose.proteus.type;

import io.github.kaktushose.proteus.type.internal.Java;
import io.github.kaktushose.proteus.type.internal.Specific;
import io.github.kaktushose.proteus.type.internal.Universal;
import org.jetbrains.annotations.NotNull;

public sealed interface Type<T> permits Java, Specific, Universal {

    static <T> Type<T> universal(@NotNull Class<T> klass) {
        return new Universal<>(klass);
    }

    static <T> Type<T> specific(@NotNull String entity, @NotNull String format, @NotNull String kind, @NotNull Class<T> container) {
        return new Specific<>(entity, format, kind, container);
    }

    static <T> Type<T> of(@NotNull Class<T> klass) {
        return new Java<>(new TypeReference<>(klass) {});
    }

    static <T> Type<T> of(@NotNull TypeReference<T> reference) {
        return new Java<>(reference);
    }
}