package io.github.kaktushose.proteus.adapter.impl;

import io.github.kaktushose.proteus.adapter.ReversibleTypAdapter;

import java.util.Optional;

public class ShortAdapter implements ReversibleTypAdapter<Integer, Short> {

    @Override
    public Optional<Short> apply(Integer source) {
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
