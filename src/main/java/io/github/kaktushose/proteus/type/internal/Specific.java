package io.github.kaktushose.proteus.type.internal;

import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record Specific<T>(@NotNull String entity, @NotNull String format, @NotNull String kind,
                          @NotNull Class<T> container) implements Type<T> {

    public Specific {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(format);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(container);
    }

    public boolean equalsIgnoreContainer(@Nullable io.github.kaktushose.proteus.type.internal.Specific<?> target) {
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
