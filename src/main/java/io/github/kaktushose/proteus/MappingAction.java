package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

public final class MappingAction<S> {

    private final Type<S> source;
    private final Proteus proteus;

    public MappingAction(@NotNull Type<S> source, @NotNull Proteus proteus) {
        this.source = source;
        this.proteus = proteus;
    }

    @NotNull
    public <T> MappingAction<S> to(@NotNull Type<T> target, @NotNull Mapper<S, T> mapper) {
        return to(target, mapper, proteus.conflictStrategy());
    }

    @NotNull
    public <T> MappingAction<S> to(@NotNull Type<T> target, @NotNull Mapper<S, T> mapper, @NotNull ProteusBuilder.ConflictStrategy strategy) {
        proteus.register(source, target, mapper, strategy);
        return this;
    }
}
