package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

/// A [MappingAction] is returned by [Proteus#map(Type)] and used to register one or, if needed, multiple [Mapper]s for
/// the [Type] that is bound to this [MappingAction].
///
/// @param <S> the type of the [Type] that is bound to this [MappingAction]
public final class MappingAction<S> {

    private final Type<S> source;
    private final Proteus proteus;

    MappingAction(@NotNull Type<S> source, @NotNull Proteus proteus) {
        this.source = source;
        this.proteus = proteus;
    }

    /// Registers the given [Mapper] for the provided target [Type]. This will use the configured [Proteus#conflictStrategy()]
    /// of the underlying proteus instance.
    ///
    /// @param target the target [Type]
    /// @param mapper the [Mapper] to register
    /// @param <T>    the type of the target [Type]
    /// @return this instance for fluent interface
    @NotNull
    public <T> MappingAction<S> to(@NotNull Type<T> target, @NotNull Mapper<S, T> mapper) {
        return to(target, mapper, proteus.conflictStrategy());
    }

    /// Registers the given [Mapper] for the provided target [Type]. This will use the given [ProteusBuilder.ConflictStrategy]
    /// overriding the configured [Proteus#conflictStrategy()] of the underlying proteus instance.
    ///
    /// @param target   the target [Type]
    /// @param mapper   the [Mapper] to register
    /// @param strategy the [ProteusBuilder.ConflictStrategy] to use if the `from` [Type] is already registered
    /// @param <T>      the type of the target [Type]
    /// @return this instance for fluent interface
    @NotNull
    public <T> MappingAction<S> to(@NotNull Type<T> target, @NotNull Mapper<S, T> mapper, @NotNull ProteusBuilder.ConflictStrategy strategy) {
        proteus.register(source, target, mapper, strategy);
        return this;
    }
}
