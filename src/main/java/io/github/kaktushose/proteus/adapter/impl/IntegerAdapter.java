package io.github.kaktushose.proteus.adapter.impl;

import io.github.kaktushose.proteus.adapter.ReversibleTypAdapter;

import java.util.Optional;

public class IntegerAdapter implements ReversibleTypAdapter<Long, Integer> {

    @Override
    public Optional<Integer> apply(Long source) {
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
