package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.mapping.Flag;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/// A [IntoMappingAction] is returned by [Proteus#into(Type)] and used to register one or, if needed, multiple [Mapper]s for
/// the [Type] that is bound to this [IntoMappingAction].
///
/// @param <T> the type of the [Type] that is bound to this [IntoMappingAction]
public final class IntoMappingAction<T> {

    private final List<Type<? extends T>> sources;
    private final Proteus proteus;

    IntoMappingAction(@NotNull List<Type<? extends T>> sources, @NotNull Proteus proteus) {
        this.sources = sources;
        this.proteus = proteus;
    }

    /// Registers the given [Mapper] for the provided source [Type]. This will use the configured [Proteus#conflictStrategy()]
    /// of the underlying proteus instance.
    ///
    /// @param source the source [Type]
    /// @param mapper the [Mapper] to register
    /// @param flags    the [Flag]s to register this mapper with
    /// @param <S>    the type of the source [Type]
    /// @return this instance for fluent interface
    @NotNull
    public <S> IntoMappingAction<T> from(@NotNull Type<S> source, @NotNull Mapper<S, T> mapper, @NotNull Flag... flags) {
        return from(source, mapper, proteus.conflictStrategy(), flags);
    }

    /// Registers the given [Mapper] for the provided source [Type]. This will use the given [ProteusBuilder.ConflictStrategy]
    /// overriding the configured [Proteus#conflictStrategy()] of the underlying proteus instance.
    ///
    /// @param source   the source [Type]
    /// @param mapper   the [Mapper] to register
    /// @param strategy the [ProteusBuilder.ConflictStrategy] to use if the `from` [Type] is already registered
    /// @param flags    the [Flag]s to register this mapper with
    /// @param <S>      the type of the source [Type]
    /// @return this instance for fluent interface
    @NotNull
    @SuppressWarnings("unchecked")
    public <S> IntoMappingAction<T> from(@NotNull Type<S> source,
                                         @NotNull Mapper<S, T> mapper,
                                         @NotNull ProteusBuilder.ConflictStrategy strategy,
                                         @NotNull Flag... flags) {
        sources.forEach(target -> proteus.register(source, (Type<T>) target, mapper, strategy, flags));
        return this;
    }
}
