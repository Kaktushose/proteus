package io.github.kaktushose.proteus.type;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface Format {

    boolean equals(Format format);

    static None none() {
        return new None();
    }

    static Default of(@NotNull String entity, @NotNull String format, @NotNull String kind) {
        return new Default(entity, format, kind);
    }

    record Default(@NotNull String entity, @NotNull String format, @NotNull String kind) implements Format {

        public Default {
            Objects.requireNonNull(entity);
            Objects.requireNonNull(format);
            Objects.requireNonNull(kind);
        }

        @Override
        public boolean equals(Format format) {
            if (format instanceof Default(String otherEntity, String otherFormat, String otherKind)) {
                return Objects.equals(this.entity, otherEntity) && Objects.equals(this.format, otherFormat) && Objects.equals(this.kind, otherKind);
            }
            return false;
        }
    }

    record None() implements Format {

        @Override
        public boolean equals(Format format) {
            return false;
        }
    }
}
