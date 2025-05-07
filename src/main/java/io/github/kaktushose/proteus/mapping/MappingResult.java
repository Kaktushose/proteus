package io.github.kaktushose.proteus.mapping;

import org.jetbrains.annotations.NotNull;

public sealed interface MappingResult<T> {

    @NotNull
    static <T> Success<T> success(@NotNull T value) {
        return new Success<>(value);
    }

    @NotNull
    static <T> Failure<T> failure(@NotNull String message) {
        return new Failure<>(message);
    }

    record Success<T>(@NotNull T value) implements MappingResult<T> {}

    record Failure<T>(@NotNull String message) implements MappingResult<T> {}

}
