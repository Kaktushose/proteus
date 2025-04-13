package io.github.kaktushose.proteus.conversion;

public sealed interface Result<T> {

    record Success<T>(T value) implements Result<T> {}

    record Failure<T>(String message) implements Result<T> {}

    static <T> Success<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Failure<T> failure(String message) {
        return new Failure<>(message);
    }

}
