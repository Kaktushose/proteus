package io.github.kaktushose.proteus.conversion.type;

public record Type<T>(String entity, String format, String kind, Class<T> container) {}
