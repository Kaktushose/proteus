package io.github.kaktushose.proteus.mapping;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/// A mapper is responsible for converting a source type `S` into a target type `T`.
///
/// @param <S> the source type
/// @param <T> the target type
public sealed interface Mapper<S, T> {

    /// Creates an [UniMapper] that can convert from a source type `S` into a target type `T`, but doesn't have to
    /// guarantee that data may be lost during conversion (lossy conversion).
    ///
    /// @param mapper the [BiFunction] that converts the source type into the target type
    /// @param <S>    the source type
    /// @param <T>    the target type
    /// @return a new [UniMapper]
    static <S, T> UniMapper<S, T> lossy(@NotNull BiFunction<S, MappingContext, MappingResult<T>> mapper) {
        return new UniMapper<>() {
            @Override
            public @NotNull MappingResult<T> from(@NotNull S source, @NotNull MappingContext context) {
                return mapper.apply(source, context);
            }

            @Override
            public boolean lossless() {
                return false;
            }
        };
    }

    /// Creates an [UniMapper] that can convert from a source type `S` into a target type `T`, that guarantees
    /// that **no** data will be lost during conversion (lossless conversion).
    ///
    /// @param mapper the [BiFunction] that converts the source type into the target type
    /// @param <S>    the source type
    /// @param <T>    the target type
    /// @return a new [UniMapper]
    static <S, T> UniMapper<S, T> lossless(@NotNull BiFunction<S, MappingContext, MappingResult<T>> mapper) {
        return new UniMapper<>() {
            @Override
            public @NotNull MappingResult<T> from(@NotNull S source, @NotNull MappingContext context) {
                return mapper.apply(source, context);
            }

            @Override
            public boolean lossless() {
                return true;
            }
        };
    }

    /// Creates an [BiMapper] that can convert from a source type `S` into a target type `T`, but can also reversely
    /// convert from the target type `T` into the source type `S`. This is always a lossless conversion, meaning
    /// **no** data must be lost during conversion.
    ///
    /// @param from the [BiFunction] that converts the source type into the target type
    /// @param into the [BiFunction] that converts the target type into the source type
    /// @param <S>  the source type
    /// @param <T>  the target type
    /// @return a new [UniMapper]
    static <S, T> BiMapper<S, T> lossless(@NotNull BiFunction<S, MappingContext, MappingResult<T>> from, @NotNull BiFunction<T, MappingContext, MappingResult<S>> into) {
        return new BiMapper<>() {

            @Override
            public @NotNull MappingResult<T> from(@NotNull S source, @NotNull MappingContext context) {
                return from.apply(source, context);
            }

            @Override
            public @NotNull MappingResult<S> into(@NotNull T target, @NotNull MappingContext context) {
                return into.apply(target, context);
            }
        };
    }

    /// A subtype of [Mapper] that can convert the source type `S` into the target type `T`.
    ///
    /// @param <S> the source type
    /// @param <T> the target type
    non-sealed interface UniMapper<S, T> extends Mapper<S, T> {

        /// @param source  the source [S] to convert from
        /// @param context the [MappingContext] providing additional information
        /// @return the target [T] to convert into wrapped in a [MappingResult]
        @NotNull
        MappingResult<T> from(@NotNull S source, @NotNull MappingContext context);

        boolean lossless();

    }

    /// A subtype of [Mapper] that can convert the source type `S` into the target type `T`, but can also convert the
    /// target type `T` back into the source type `S`.
    ///
    /// @param <S> the source type
    /// @param <T> the target type
    non-sealed interface BiMapper<S, T> extends Mapper<S, T> {

        /// @param source  the source [S] to convert from
        /// @param context the [MappingContext] providing additional information
        /// @return the target [T] to convert into wrapped in a [MappingResult]
        @NotNull
        MappingResult<T> from(@NotNull S source, @NotNull MappingContext context);

        /// @param target  the target [T] to convert from
        /// @param context the [MappingContext] providing additional information
        /// @return the source [S] to convert back into wrapped in a [MappingResult]
        @NotNull
        MappingResult<S> into(@NotNull T target, @NotNull MappingContext context);
    }

    /// TBD
    record MappingContext() {}
}
