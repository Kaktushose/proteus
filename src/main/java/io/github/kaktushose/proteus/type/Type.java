package io.github.kaktushose.proteus.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public sealed interface Type<T> {

    record Class<T>(@NotNull java.lang.Class<T> clazz) implements Type<T> {

        public Class {
            Objects.requireNonNull(clazz);
        }
    }

    record Parameterized<T>(@NotNull ParameterizedTypeReference<T> reference) implements Type<T> {

        public Parameterized {
            Objects.requireNonNull(reference);
        }
    }

    record Universal<T>(@NotNull java.lang.Class<T> clazz) implements Type<T> {

        public Universal {
            Objects.requireNonNull(clazz);
        }
    }

    record Specific<T>(@NotNull String entity, @NotNull String format, @NotNull String kind, @NotNull java.lang.Class<T> container) implements Type<T> {

        public Specific {
            Objects.requireNonNull(entity);
            Objects.requireNonNull(format);
            Objects.requireNonNull(kind);
            Objects.requireNonNull(container);
        }

        public boolean equalsIgnoreContainer(@Nullable Specific<?> target) {
            if (target == null) {
                return false;
            }
            return Objects.equals(entity, target.entity) && Objects.equals(format, target.format) && Objects.equals(kind, target.kind);
        }

        @NotNull
        public Universal<T> universal() {
            return new Universal<>(container);
        }
    }
}