package io.github.kaktushose.proteus.conversion;

import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.graph.Edge;
import io.github.kaktushose.proteus.graph.Edge.ResolvedEdge;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Represents the final result of a type conversion.
///
/// # Example:
/// ```
/// switch (result) {
///     case ConversionResult.Success<Object>(Object success) -> ...; // proceed with converted object
///     case ConversionResult.Failure<Object> failure -> log(failure);
/// }
/// ```
///
/// @param <T> the type of the result
/// @see Proteus#convert(Object, Type, Type)
public sealed interface ConversionResult<T> {

    /// Creates a new [ConversionResult] with the given input.
    ///
    /// @param result    the [MappingResult] to create the [ConversionResult] from. This is either a [MappingResult.Success] from
    ///                  the very last step or a [MappingResult.Failure] from any step that failed
    /// @param errorType the [Failure.ErrorType] to use if the given [MappingResult] is a [MappingResult.Failure]
    /// @param context   the [ConversionContext], can be null if this [ConversionResult] wasn't created during conversion
    /// @param <T>       <T> the type of the result
    /// @return the newly created [ConversionResult]
    @NotNull
    @SuppressWarnings("unchecked")
    static <T> ConversionResult<T> of(@NotNull MappingResult<T> result, @NotNull Failure.ErrorType errorType, @Nullable ConversionContext context) {
        return switch (result) {
            case MappingResult.Success<T>(Object success) -> new Success<>((T) success);
            case MappingResult.Failure<T>(String message) -> new Failure<>(errorType, message, context);
        };
    }

    /// Implementation of [ConversionResult] that indicates a successful conversion.
    ///
    /// @param value the value of the result
    /// @param <T>   the type of the result
    record Success<T>(@NotNull T value) implements ConversionResult<T> {}

    /// Implementation of [ConversionResult] that indicates a failed conversion.
    ///
    /// @param errorType the [Failure.ErrorType] of this failed conversion
    /// @param message   an error message describing the failed conversion
    /// @param context   the [ConversionContext], can be null if this [Failure] wasn't created during conversion, e.g. if no path was found
    /// @param <T>       the type of the result
    record Failure<T>(@NotNull ErrorType errorType, @NotNull String message, @Nullable ConversionContext context) implements ConversionResult<T> {

        /// Gets a detailed error message showing the full path and which step failed.
        ///
        /// @return a detailed error message
        @NotNull
        public String detailedMessage() {
            if (context == null) {
                return message;
            }
            List<Edge> path = context.path();
            ResolvedEdge step = context.step();
            Type<?> from = step.from();
            Type<?> into = step.into();
            int index = path.indexOf(step);
            StringBuilder error = new StringBuilder();
            error.append("Failed to convert from '%s' to '%s'\n".formatted(context.from(), context.into()))
                    .append("Reason:\n     %s(message=%s)\n".formatted(errorType, message))
                    .append("Step:\n     '%s' -> '%s':\n".formatted(from, into))
                    .append("Path:\n");
            for (int i = 0; i < path.size(); i++) {
                var edge = path.get(i);
                if (i == index) {
                    error.append("  -> %s".formatted(edge.from())).append("\n").append("  -> %s".formatted(edge.into()));
                } else {
                    error.append("     ").append(edge.from()).append("\n").append("     ").append(edge.into());
                }
                error.append("\n");
            }
            return error.toString();
        }

        /// An enum describing the different error types.
        public enum ErrorType {
            /// Indicates that no path was found to convert from `Type A` to `Type B`.
            NO_PATH_FOUND,
            /// Indicates that a path was found but mapping failed for one of the steps.
            MAPPING_FAILED,
            /// Indicates that a path was found but for one of the steps no lossless mapper was found-
            NO_LOSSLESS_CONVERSION
        }
    }

    /// Provides additional information about the conversion that failed.
    ///
    /// @param path a [List] of [Edge]s describing the full path of the conversion
    /// @param step the [ResolvedEdge] at which the conversion failed
    record ConversionContext(@NotNull List<Edge> path, @NotNull ResolvedEdge step) {

        /// Gets the source [Type] of the path.
        ///
        /// @return the source [Type] of the path
        @NotNull
        public Type<?> from() {
            return path.getFirst().from();
        }

        /// Gets the destination [Type] of the path.
        ///
        /// @return the destination [Type] of the path
        @NotNull
        public Type<?> into() {
            return path.getLast().into();
        }
    }
}
