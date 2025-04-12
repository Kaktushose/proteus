package io.github.kaktushose.proteus.adapter.impl;

import io.github.kaktushose.proteus.adapter.ReversibleTypAdapter;

import java.util.Optional;

public class LongAdapter implements ReversibleTypAdapter<String, Long> {

    @Override
    public Optional<Long> apply(String source) {
        try {
            return Optional.of(Long.valueOf(source));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> reverse(Long target) {
        return Optional.of(String.valueOf(target));
    }
}
