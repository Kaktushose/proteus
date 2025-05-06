package io.github.kaktushose.proteus.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record Type<T>(@NotNull Format format, @NotNull TypeReference<T> container) {

    public Type {
        Objects.requireNonNull(format);
        Objects.requireNonNull(container);
    }

    public static <T> Type<T> of(@NotNull String entity, @NotNull String format, @NotNull String kind, @NotNull Class<T> container) {
        return new Type<>(Format.of(entity, format, kind), new TypeReference<>(container) {});
    }

    public static <T> Type<T> of(@NotNull Class<T> klass) {
        return new Type<>(Format.none(),  new TypeReference<>(klass) {});
    }

    public static <T> Type<T> of(@NotNull TypeReference<T> reference) {
        return new Type<>(Format.none(), reference);
    }

    public boolean equalsIgnoreContainer(@Nullable Type<?> target) {
        if (target == null) {
            return false;
        }
        if (format instanceof Format.None) {
            return false;
        }
        return format.equals(target.format);
    }
}
