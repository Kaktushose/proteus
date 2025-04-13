package io.github.kaktushose.proteus.conversion.type;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Type<T> {

    private final String entity;
    private final String format;
    private final String kind;
    private final Class<T> container;

    private Type(String entity, String format, String kind, Class<T> container) {
        this.entity = entity;
        this.format = format;
        this.kind = kind;
        this.container = container;
    }

    public static <T> Type<T> of(@NotNull String entity, @NotNull String format, @NotNull String kind, @NotNull Class<T> container) {
        return new Type<>(
                Objects.requireNonNull(entity),
                Objects.requireNonNull(format),
                Objects.requireNonNull(kind),
                Objects.requireNonNull(container)
        );
    }

    public static <T> Type<T> universal(@NotNull Class<T> clazz) {
        return new Type<>(clazz.getName(), null, null, Objects.requireNonNull(clazz));
    }

    public String entity() {
        return entity;
    }

    public String format() {
        return format;
    }

    public String kind() {
        return kind;
    }

    public Class<T> container() {
        return container;
    }

    public boolean isUniversal() {
        return format == null && kind == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Type<?>) obj;
        return Objects.equals(this.entity, that.entity) &&
               Objects.equals(this.format, that.format) &&
               Objects.equals(this.kind, that.kind) &&
               Objects.equals(this.container, that.container);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, format, kind, container);
    }

    @Override
    public String toString() {
        return "Type[" +
               "entity=" + entity + ", " +
               "format=" + format + ", " +
               "kind=" + kind + ", " +
               "container=" + container + ']';
    }
}
