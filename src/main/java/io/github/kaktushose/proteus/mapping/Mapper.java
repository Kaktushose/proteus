package io.github.kaktushose.proteus.mapping;

import io.github.kaktushose.proteus.graph.Edge;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public sealed interface Mapper<S, T> {

    static <S, T> UniMapper<S, T> lossy(@NotNull BiFunction<S, MappingContext, MappingResult<T>> mapper) {
        return new UniMapper<>() {
            @Override
            public @NotNull MappingResult<T> from(@NotNull S source, @NotNull MappingContext context) {
                return mapper.apply(source, context);
            }

            @Override
            public boolean lossless() {
                return false;
            }
        };
    }

    static <S, T> UniMapper<S, T> lossless(@NotNull BiFunction<S, MappingContext, MappingResult<T>> mapper) {
        return new UniMapper<>() {
            @Override
            public @NotNull MappingResult<T> from(@NotNull S source, @NotNull MappingContext context) {
                return mapper.apply(source, context);
            }

            @Override
            public boolean lossless() {
                return true;
            }
        };
    }

    static <S, T> BiMapper<S, T> lossless(@NotNull BiFunction<S, MappingContext, MappingResult<T>> from, @NotNull BiFunction<T, MappingContext, MappingResult<S>> into) {
        return new BiMapper<>() {

            @Override
            public @NotNull MappingResult<T> from(@NotNull S source, @NotNull MappingContext context) {
                return from.apply(source, context);
            }

            @Override
            public @NotNull MappingResult<S> into(@NotNull T target, @NotNull MappingContext context) {
                return into.apply(target, context);
            }
        };
    }

    non-sealed interface UniMapper<S, T> extends Mapper<S, T> {
        @NotNull
        MappingResult<T> from(@NotNull S source, @NotNull MappingContext context);

        boolean lossless();

    }

    non-sealed interface BiMapper<S, T> extends Mapper<S, T> {
        @NotNull
        MappingResult<T> from(@NotNull S source, @NotNull MappingContext context);

        @NotNull
        MappingResult<S> into(@NotNull T target, @NotNull MappingContext context);
    }

    record MappingContext(@NotNull List<Edge> path, @NotNull Edge.ResolvedEdge step) {

        public Type<?> from() {
            return path.getFirst().from();
        }

        public Type<?> into() {
            return path.getLast().into();
        }

    }
}
