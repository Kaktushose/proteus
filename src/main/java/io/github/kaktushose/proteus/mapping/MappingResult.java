package io.github.kaktushose.proteus.mapping;

import io.github.kaktushose.proteus.conversion.ConversionResult;
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

    @NotNull
    @SuppressWarnings("unchecked")
    static <T> MappingResult<T> of(@NotNull ConversionResult<T> result) {
        return switch (result) {
            case ConversionResult.Success<T>(Object success) -> (MappingResult<T>) success(success);
            case ConversionResult.Failure<T> failure -> failure(failure.message());
        };
    }

    record Success<T>(@NotNull T value) implements MappingResult<T> {}

    record Failure<T>(@NotNull String message) implements MappingResult<T> {}

}
