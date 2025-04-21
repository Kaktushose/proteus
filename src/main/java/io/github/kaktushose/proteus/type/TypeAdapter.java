package io.github.kaktushose.proteus.type;

import io.github.kaktushose.proteus.conversion.Mapper;
import org.jetbrains.annotations.NotNull;

public record TypeAdapter<S, T>(@NotNull Type<S> source, @NotNull Type<T> target, @NotNull Mapper mapper) {


    public TypeAdapter(@NotNull Type<S> source, @NotNull Type<T> target, @NotNull Mapper.UniMapper<S, T> mapper) {
        this(source, target, (Mapper) mapper);
    }

    public TypeAdapter(@NotNull Type<S> source, @NotNull Type<T> target, @NotNull Mapper.BiMapper<S, T> mapper) {
        this(source, target, (Mapper) mapper);
    }

}
