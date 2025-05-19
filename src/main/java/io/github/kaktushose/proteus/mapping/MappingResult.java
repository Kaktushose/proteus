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

    /// Creates a new successful, lossless [MappingResult]
    ///
    /// @param value the result [T] of the mapping
    /// @param <T>   the type of the result
    /// @return a [Lossless]
    @NotNull
    static <T> Lossless<T> lossless(@NotNull T value) {
        return new Lossless<>(value);
    }

    /// Creates a new successful, lossy [MappingResult]
    ///
    /// @param value the result [T] of the mapping
    /// @param <T>   the type of the result
    /// @return a [Lossless]
    @NotNull
    static <T> Lossy<T> lossy(@NotNull T value) {
        return new Lossy<>(value);
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
    static <T> MappingResult<T> of(@NotNull ConversionResult<T> result) {
        return switch (result) {
            case ConversionResult.Success<T>(T success, boolean lossless) when lossless -> lossless(success);
            case ConversionResult.Success<T>(T success, boolean _) -> lossy(success);
            case ConversionResult.Failure<T> failure -> failure(failure.message());
        };
    }

    /// Common type of [MappingResult] for the successful result types [Lossless] and [Lossy].
    ///
    /// @param <T> the type of the result
    sealed interface Successful<T> extends MappingResult<T> permits Lossless, Lossy {
        /// Returns the result of the successful mapping
        ///
        /// @return the result of the successful mapping
        T value();
    }

    /// Implementation of [MappingResult] that indicates a successful mapping, where no data was lost during mapping.
    ///
    /// @param value the result of the successful mapping
    /// @param <T>   the type of the result
    record Lossless<T>(@NotNull T value) implements Successful<T> {}

    /// Implementation of [MappingResult] that indicates a successful mapping, where data may be lost during mapping.
    ///
    /// @param value the result of the successful mapping
    /// @param <T>   the type of the result
    record Lossy<T>(@NotNull T value) implements Successful<T> {}

    /// Implementation of [MappingResult] that indicates a failed mapping.
    ///
    /// @param message an error message describing the failed mapping
    /// @param <T>     the type of the result
    record Failure<T>(@NotNull String message) implements MappingResult<T> {}

}
