package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/// A [FromMappingAction] is returned by [Proteus#from(Type)] and used to register one or, if needed, multiple [Mapper]s for
/// the [Type] that is bound to this [FromMappingAction].
///
/// @param <S> the type of the [Type] that is bound to this [FromMappingAction]
public final class FromMappingAction<S> {

    private final List<Type<? extends S>> sources;
    private final Proteus proteus;

    FromMappingAction(@NotNull List<Type<? extends S>> sources, @NotNull Proteus proteus) {
        this.sources = sources;
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
    public <T> FromMappingAction<S> into(@NotNull Type<T> target, @NotNull Mapper<S, T> mapper) {
        return into(target, mapper, proteus.conflictStrategy());
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
    @SuppressWarnings("unchecked")
    public <T> FromMappingAction<S> into(@NotNull Type<T> target, @NotNull Mapper<S, T> mapper, @NotNull ProteusBuilder.ConflictStrategy strategy) {
        sources.forEach(source -> proteus.register((Type<S>) source, target, mapper, strategy));
        return this;
    }
}
