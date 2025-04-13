package io.github.kaktushose.proteus.conversion.type;

import io.github.kaktushose.proteus.conversion.Mapper;

public record TypeAdapter<S, T>(Type<S> source, Type<T> target, Mapper mapper) {

    public TypeAdapter(Type<S> source, Type<T> target, Mapper.UniMapper<S, T> mapper) {
        this(source, target, (Mapper) mapper);
    }

    public TypeAdapter(Type<S> source, Type<T> target, Mapper.BiMapper<S, T> mapper) {
        this(source, target, (Mapper) mapper);
    }

}
