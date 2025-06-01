package io.github.kaktushose.proteus.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodType;
import java.util.Objects;

/// Representation of a type that can be converted from and into.
///
/// @param format    the [Format] describing this type
/// @param container the [TypeReference] used to hold the data of this type
/// @param <T>       the type of the [TypeReference]
public record Type<T>(@NotNull Format format, @NotNull TypeReference<T> container) {

    public Type {
        Objects.requireNonNull(format);
        Objects.requireNonNull(container);
    }

    /// Create a new [Type] with the container retrieved from [Object#getClass()] called on `value`
    /// that will have the [Format.None].
    ///
    /// @param value the object [Object#getClass()] should be called on
    /// @return a new [Type] with [Format.None] and the container defined by `value`
    public static Type<Object> dynamic(Object value) {
        return new Type<>(Format.none(), new TypeReference<>(value.getClass()) {});
    }

    /// Creates a new [Type] with the given [Format] and container [Class]. Primitive types will be converted to their
    /// corresponding wrapper types.
    ///
    /// @param format    the [Format] of the [Type]
    /// @param container the [Class] that will be used to hold the data of the [Type]
    /// @param <T>       the type of the container [Class]
    /// @return a new [Type] with the given [Format] and container [Class]
    @NotNull
    public static <T> Type<T> of(@NotNull Format format, @NotNull Class<T> container) {
        return new Type<>(format, new TypeReference<>(wrap(container)) {});
    }

    /// Creates a new [Type] with the given container [Class] that will have the [Format.None]. Primitive types will be
    ///  converted to their corresponding wrapper types.
    ///
    /// @param container the [Class] that will be used to hold the data of the [Type]
    /// @param <T>       the type of the container [Class]
    /// @return a new [Type] with [Format.None] and the given container [Class]
    @NotNull
    public static <T> Type<T> of(@NotNull Class<T> container) {
        return new Type<>(Format.none(), new TypeReference<>(wrap(container)) {});
    }

    /// Creates a new [Type] with the given container [TypeReference] that will have the [Format.None].
    ///
    /// @param container the [TypeReference] that will be used to hold the data of the [Type]
    /// @param <T>       the type of the container [TypeReference]
    /// @return a new [Type] with the given [Format] and the [Format.None]
    @NotNull
    public static <T> Type<T> of(@NotNull TypeReference<T> container) {
        return new Type<>(Format.none(), container);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> wrap(Class<T> c) {
        return (Class<T>) MethodType.methodType(c).wrap().returnType();
    }

    /// Whether this type is equals to the given [Type] ignoring the container [TypeReference] and only comparing the
    /// [Format]s. This will return `false` if one of the [Format]s is of [Format.None].
    ///
    /// @param other the [Type] to compare against
    /// @return `true` if the two [Type]s have the same [Format]
    public boolean equalsFormat(@Nullable Type<?> other) {
        if (other == null) {
            return false;
        }
        if (format instanceof Format.None || other.format() instanceof Format.None) {
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
