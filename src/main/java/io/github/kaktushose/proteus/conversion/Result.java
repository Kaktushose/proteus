package io.github.kaktushose.proteus.conversion;

import org.jetbrains.annotations.NotNull;

public sealed interface Result<T> {

    @NotNull
    static <T> Success<T> success(@NotNull T value) {
        return new Success<>(value);
    }

    @NotNull
    static <T> Failure<T> failure(@NotNull String message) {
        return new Failure<>(message);
    }

    record Success<T>(@NotNull T value) implements Result<T> {}

    record Failure<T>(@NotNull String message) implements Result<T> {}

}
