package io.github.kaktushose.proteus.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Representation of a type that can be converted from and into.
///
/// @param format the [Format] describing this type
/// @param container the [TypeReference] used to hold the data of this type
/// @param <T> the type of the [TypeReference]
public record Type<T>(@NotNull Format format, @NotNull TypeReference<T> container) {

    public Type {
        Objects.requireNonNull(format);
        Objects.requireNonNull(container);
    }

    /// Creates a new [Type] with the given [Format] and container [Class].
    ///
    /// @param format the [Format] of the [Type]
    /// @param container the [Class] that will be used to hold the data of the [Type]
    /// @return a new [Type] with the given [Format] and container [Class]
    /// @param <T> the type of the container [Class]
    @NotNull
    public static <T> Type<T> of(@NotNull Format format, @NotNull Class<T> container) {
        return new Type<>(format, new TypeReference<>(container) {});
    }

    /// Creates a new [Type] with the given container [Class] that will have the [Format.None].
    ///
    /// @param container the [Class] that will be used to hold the data of the [Type]
    /// @return a new [Type] with the given [Format] and the [Format.None]
    /// @param <T> the type of the container [Class]
    @NotNull
    public static <T> Type<T> of(@NotNull Class<T> container) {
        return new Type<>(Format.none(), new TypeReference<>(container) {});
    }

    /// Creates a new [Type] with the given container [TypeReference] that will have the [Format.None].
    ///
    /// @param container the [TypeReference] that will be used to hold the data of the [Type]
    /// @return a new [Type] with the given [Format] and the [Format.None]
    /// @param <T> the type of the container [TypeReference]
    @NotNull
    public static <T> Type<T> of(@NotNull TypeReference<T> container) {
        return new Type<>(Format.none(), container);
    }

    /// Whether this type is equals to the given [Type] ignoring the container [TypeReference] and only comparing the
    /// [Format]s.
    ///
    /// @param other the [Type] to compare against
    /// @return `true` if the two [Type]s have the same [Format]
    public boolean equalsFormat(@Nullable Type<?> other) {
        if (other == null) {
            return false;
        }
        if (other.format() instanceof Format.None) {
            return false;
        }
        return format.equals(other.format());
    }

    @NotNull
    @Override
    public String toString() {
        return "%s(%s)".formatted(format, container);
    }
}
