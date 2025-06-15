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
public record Type<T>(@NotNull Format format, @NotNull TypeReference<T> container, boolean enforceStrictMode) {

    public Type {
        Objects.requireNonNull(format);
        Objects.requireNonNull(container);
    }

    /// Create a new [Type] with the container retrieved from [Object#getClass()] called on `value`
    /// that will have the [Format#NONE].
    ///
    /// @param value the object [Object#getClass()] should be called on
    /// @return a new [Type] with [Format#NONE] and the container defined by `value`
    public static Type<Object> dynamic(@NotNull Object value) {
        Objects.requireNonNull(value);
        return new Type<>(Format.NONE, new TypeReference<>(value.getClass()) {}, false);
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
        return new Type<>(format, new TypeReference<>(wrap(container)) {}, false);
    }

    /// Creates a new [Type] with the given container [Class] that will have the [Format#NONE]. Primitive types will be
    ///  converted to their corresponding wrapper types.
    ///
    /// @param container the [Class] that will be used to hold the data of the [Type]
    /// @param <T>       the type of the container [Class]
    /// @return a new [Type] with [Format#NONE] and the given container [Class]
    @NotNull
    public static <T> Type<T> of(@NotNull Class<T> container) {
        return new Type<>(Format.NONE, new TypeReference<>(wrap(container)) {}, false);
    }

    /// Creates a new [Type] with the given container [TypeReference] that will have the [Format#NONE].
    ///
    /// @param container the [TypeReference] that will be used to hold the data of the [Type]
    /// @param <T>       the type of the container [TypeReference]
    /// @return a new [Type] with the given [Format] and the [Format#NONE]
    @NotNull
    public static <T> Type<T> of(@NotNull TypeReference<T> container) {
        return new Type<>(Format.NONE, container, false);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> wrap(Class<T> c) {
        return (Class<T>) MethodType.methodType(c).wrap().returnType();
    }

    /// Whether this type is equals to the given [Type] ignoring the container [TypeReference] and only comparing the
    /// [Format]s. This will return `false` if one of the [Format]s is of [Format#NONE].
    ///
    /// @param other the [Type] to compare against
    /// @return `true` if the two [Type]s have the same [Format]
    public boolean equalsFormat(@Nullable Type<?> other) {
        if (other == null) {
            return false;
        }
        if (format == Format.NONE || other.format() == Format.NONE) {
            return false;
        }
        return format.equals(other.format());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Type<?> type)) return false;
        return Objects.equals(format, type.format) && Objects.equals(container, type.container);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, container);
    }

    @NotNull
    @Override
    public String toString() {
        return "%s(%s)".formatted(format == Format.NONE ? "" : format, container);
    }
}
