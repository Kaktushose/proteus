package io.github.kaktushose.proteus.adapter.impl;

import io.github.kaktushose.proteus.adapter.TypeAdapter;

import java.util.Optional;

public class IntegerAdapter implements TypeAdapter<Long, Integer> {

    @Override
    public Optional<Integer> adapt(Long source) {
        if (source > Integer.MAX_VALUE || source < Integer.MIN_VALUE) {
            return Optional.empty();
        }
        return Optional.of((int) ((long) source));
    }

    @Override
    public Optional<Long> reverse(Integer target) {
        return Optional.of(Long.valueOf(target));
    }
}
