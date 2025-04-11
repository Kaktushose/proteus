package io.github.kaktushose.proteus.adapter.impl;

import io.github.kaktushose.proteus.adapter.TypeAdapter;

import java.util.Optional;

public class ShortAdapter implements TypeAdapter<Integer, Short> {

    @Override
    public Optional<Short> adapt(Integer source) {
        if (source > Short.MAX_VALUE || source < Short.MIN_VALUE) {
            return Optional.empty();
        }
        return Optional.of((short) ((int) source));
    }

    @Override
    public Optional<Integer> reverse(Short target) {
        return Optional.of(Integer.valueOf(target));
    }

}
