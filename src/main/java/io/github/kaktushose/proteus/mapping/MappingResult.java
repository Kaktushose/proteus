package io.github.kaktushose.proteus.mapping;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper.BiMapper;
import io.github.kaktushose.proteus.mapping.Mapper.UniMapper;
import org.jetbrains.annotations.NotNull;

/// Represents the result of a [Mapper] call.
///
/// @param <T> the type of the result
/// @see UniMapper#from(Object, Mapper.MappingContext)
/// @see BiMapper#from(Object, Mapper.MappingContext)
/// @see BiMapper#into(Object, Mapper.MappingContext)
public sealed interface MappingResult<T> {

    /// Creates a new successful [MappingResult]
    ///
    /// @param value the result [T] of the mapping
    /// @param <T>   the type of the result
    /// @return a [Success]
    @NotNull
    static <T> Success<T> success(@NotNull T value) {
        return new Success<>(value);
    }

    /// Creates a new successful [MappingResult]
    ///
    /// @param message an error message describing the failed mapping
    /// @param <T>     the type of the result
    /// @return a [Failure]
    @NotNull
    static <T> Failure<T> failure(@NotNull String message) {
        return new Failure<>(message);
    }

    /// Creates a [MappingResult] from the given [ConversionResult]. In case of [ConversionResult.Failure] this will
    /// truncate additional information and only keep the error message.
    ///
    /// @param result the [ConversionResult] to create a [MappingResult] from
    /// @param <T>    the type of the result
    /// @return the [MappingResult]
    @NotNull
    @SuppressWarnings("unchecked")
    static <T> MappingResult<T> of(@NotNull ConversionResult<T> result) {
        return switch (result) {
            case ConversionResult.Success<T>(Object success) -> (MappingResult<T>) success(success);
            case ConversionResult.Failure<T> failure -> failure(failure.message());
        };
    }

    /// Implementation of [MappingResult] that indicates a successful mapping.
    ///
    /// @param value the result of the successful mapping
    /// @param <T>   the type of the result
    record Success<T>(@NotNull T value) implements MappingResult<T> {}

    /// Implementation of [MappingResult] that indicates a failed mapping.
    ///
    /// @param message an error message describing the failed mapping
    /// @param <T>     the type of the result
    record Failure<T>(@NotNull String message) implements MappingResult<T> {}

}
