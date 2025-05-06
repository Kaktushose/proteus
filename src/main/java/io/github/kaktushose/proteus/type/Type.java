package io.github.kaktushose.proteus.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public sealed interface Type<T> {

    static <T> Type<T> specific(@NotNull String entity, @NotNull String format, @NotNull String kind, @NotNull Class<T> container) {
        return new Specific<>(entity, format, kind, container);
    }

    static <T> Type<T> of(@NotNull Class<T> klass) {
        return new Java<>(new TypeReference<>(klass) {});
    }

    static <T> Type<T> of(@NotNull TypeReference<T> reference) {
        return new Java<>(reference);
    }

    record Java<T>(@NotNull TypeReference<T> reference) implements Type<T> {

        public Java {
            Objects.requireNonNull(reference);
        }
    }

    record Specific<T>(@NotNull String entity, @NotNull String format, @NotNull String kind, @NotNull Class<T> container) implements Type<T> {

        public Specific {
            Objects.requireNonNull(entity);
            Objects.requireNonNull(format);
            Objects.requireNonNull(kind);
            Objects.requireNonNull(container);
        }

        public boolean equalsIgnoreContainer(@Nullable Type.Specific<?> target) {
            if (target == null) {
                return false;
            }
            return Objects.equals(entity, target.entity) && Objects.equals(format, target.format) && Objects.equals(kind, target.kind);
        }
    }
}
