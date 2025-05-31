package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.conversion.ConversionResult.ConversionContext;
import io.github.kaktushose.proteus.conversion.CyclingConversionException;
import io.github.kaktushose.proteus.graph.Edge;
import io.github.kaktushose.proteus.graph.Graph;
import io.github.kaktushose.proteus.mapping.Flag;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.Mapper.MappingContext;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.github.kaktushose.proteus.conversion.ConversionResult.Failure.ErrorType.*;

/// The main entrypoint of the proteus library. Use this class to register [Mapper]s or the convert [Type]s.
///
/// [#global()] will return a global proteus instance that is intended to be used across multiple frameworks or libraries
/// using proteus to benefit from a shared pool of mappers. This is the intended way of using proteus.
///
/// Alternatively, call [#create()] to create a new empty instance, or [#builder()] for further configuration.
public class Proteus {

    private static final Proteus GLOBAL_INSTANCE = Proteus.create();

    private static final ThreadLocal<List<Mapper.UniMapper<Object, Object>>> callStack = ThreadLocal.withInitial(ArrayList::new);
    private final Graph graph;
    private final ProteusBuilder.ConflictStrategy conflictStrategy;

    Proteus(Graph graph, ProteusBuilder.ConflictStrategy conflictStrategy) {
        this.graph = graph;
        this.conflictStrategy = conflictStrategy;
    }

    /// Returns a new [Proteus] instance, with no [Mapper]s registered except for the default mappers
    /// (see [ProteusBuilder#defaultMappers(boolean)]).
    ///
    /// @return a new empty [Proteus] instance
    public static Proteus create() {
        return builder().build();
    }

    /// Returns a [ProteusBuilder] to create a new [Proteus] instance.
    ///
    /// @return a [ProteusBuilder]
    public static ProteusBuilder builder() {
        return new ProteusBuilder();
    }

    /// Returns the global [Proteus] instance.
    ///
    /// @return the global [Proteus] instance
    public static Proteus global() {
        return GLOBAL_INSTANCE;
    }

    /// Returns the [ProteusBuilder.ConflictStrategy] used to resolve duplicate path registration.
    ///
    /// @return the [ProteusBuilder.ConflictStrategy]
    public ProteusBuilder.ConflictStrategy conflictStrategy() {
        return conflictStrategy;
    }

    /// Adjusts the size of the underlying LRU-Cache used for caching paths. **This will create a new cache object and
    /// erase the previous one.**
    ///
    /// @param newSize the new cache size to use for the LRU-Cache
    public void reconfigureCacheSize(int newSize) {
        graph.adjustCacheSize(newSize);
    }

    /// Entrypoint for registering one or multiple [Mapper]s for the given [Type] and its subtypes.
    ///
    /// @param into       the [Type]
    /// @param additional additional [Type]s whose container types are a subtype of the container type of `into`
    /// @param <T>        the type of the [Type]
    /// @return a [FromMappingAction] to register one or multiple mappers for the given [Type]s
    @NotNull
    @SafeVarargs
    public final <T> IntoMappingAction<T> into(Type<? extends T> into, Type<? extends T>... additional) {
        var targets = new ArrayList<>(List.of(additional));
        targets.add(into);
        return new IntoMappingAction<>(targets, this);
    }

    /// Entrypoint for registering one or multiple [Mapper]s for the given [Type].
    ///
    /// @param into the [Type]
    /// @param <T>  the type of the [Type]
    /// @return a [FromMappingAction] to register one or multiple mappers for the given [Type]
    @NotNull
    public <T> IntoMappingAction<T> into(Type<T> into) {
        return new IntoMappingAction<>(List.of(into), this);
    }

    /// Entrypoint for registering one or multiple [Mapper]s for the given [Type] and its subtypes.
    ///
    /// @param from       the [Type]
    /// @param additional additional [Type]s whose container types are a subtype of the container type of `from`
    /// @param <S>        the type of the [Type]
    /// @return a [FromMappingAction] to register one or multiple mappers for the given [Type]s
    @NotNull
    @SafeVarargs
    public final <S> FromMappingAction<S> from(Type<? extends S> from, Type<? extends S>... additional) {
        var sources = new ArrayList<>(List.of(additional));
        sources.add(from);
        return new FromMappingAction<>(sources, this);
    }

    /// Entrypoint for registering one or multiple [Mapper]s for the given [Type].
    ///
    /// @param from the [Type]
    /// @param <S>  the type of the [Type]
    /// @return a [FromMappingAction] to register one or multiple mappers for the given [Type]
    @NotNull
    public <S> FromMappingAction<S> from(Type<S> from) {
        return new FromMappingAction<>(List.of(from), this);
    }

    /// Registers a new conversion path from the given source [Type] `from` into the given destination [Type] `into`.
    /// If a path with the given source [Type] already exists, will use the configured [#conflictStrategy()] of this
    /// proteus instance to resolve the conflict.
    ///
    /// @param from   the source [Type] of the conversion path
    /// @param into   the destination [Type] of the conversion path
    /// @param mapper the [Mapper] to associate with this conversion path
    /// @param flags    the [Flag]s to register this mapper with
    /// @param <S>    the type of the `from` [Type]
    /// @param <T>    the type of into `from` [Type]
    @NotNull
    public <S, T> Proteus register(@NotNull Type<S> from, @NotNull Type<T> into, @NotNull Mapper<S, T> mapper, @NotNull Flag... flags) {
        return register(from, into, mapper, conflictStrategy, flags);
    }

    /// Registers a new conversion path from the given source [Type] `from` into the given destination [Type] `into`.
    /// If a path with the given source [Type] already exists, will use the given [ProteusBuilder.ConflictStrategy] to
    /// resolve the conflict, overriding the configured [#conflictStrategy()] of this proteus instance.
    ///
    /// @param from     the source [Type] of the conversion path
    /// @param into     the destination [Type] of the conversion path
    /// @param mapper   the [Mapper] to associate with this conversion path
    /// @param strategy the [ProteusBuilder.ConflictStrategy] to use if the `from` [Type] is already registered
    /// @param flags    the [Flag]s to register this mapper with
    /// @param <S>      the type of the `from` [Type]
    /// @param <T>      the type of into `from` [Type]
    @NotNull
    public <S, T> Proteus register(@NotNull Type<S> from,
                                   @NotNull Type<T> into,
                                   @NotNull Mapper<S, T> mapper,
                                   @NotNull ProteusBuilder.ConflictStrategy strategy,
                                   @NotNull Flag... flags) {
        graph.register(from, into, mapper, strategy, flags);
        return this;
    }

    /// Attempts to convert the source [Type] with the given value [S] to the target [Type]. This will perform a lossy
    /// conversion, which means that some data might be lost during conversion. Use [#convert(Object, Type, Type, boolean)]
    /// for lossless conversion.
    ///
    /// @param value  the value to convert
    /// @param source the [Type] of the value to convert
    /// @param target the [Type] to convert into
    /// @param <S>    the source type
    /// @param <T>    the target type
    /// @return a [ConversionResult] either holding the converted value or the error
    @NotNull
    public <S, T> ConversionResult<T> convert(@NotNull S value, @NotNull Type<S> source, @NotNull Type<T> target) {
        return convert(value, source, target, false);
    }

    /// Attempts to convert the source [Type] with the given value [S] to the target [Type].
    ///
    /// @param value    the value to convert
    /// @param source   the [Type] of the value to convert
    /// @param target   the [Type] to convert into
    /// @param lossless whether to convert lossless or not
    /// @param <S>      the source type
    /// @param <T>      the target type
    /// @return a [ConversionResult] either holding the converted value or the error
    @NotNull
    @SuppressWarnings("unchecked")
    public <S, T> ConversionResult<T> convert(@NotNull S value, @NotNull Type<S> source, @NotNull Type<T> target, boolean lossless) {
        if (source.equals(target)) {
            return new ConversionResult.Success<>((T) value, true);
        }

        List<Edge> path = graph.path(source, target);
        if (path.isEmpty()) {
            return new ConversionResult.Failure<>(NO_PATH_FOUND, "Found no path to convert from '%s' to '%s'!".formatted(source, target), null);
        }

        ConversionResult<Object> intermediate = new ConversionResult.Success<>(value, true);
        for (Edge edge : path) {
            if (intermediate instanceof ConversionResult.Success<?>(Object success, boolean _)) {
                intermediate = applyEdge(edge, path, success, lossless);
            }
        }
        return (ConversionResult<T>) intermediate;
    }

    @NotNull
    private ConversionResult<Object> applyEdge(@NotNull Edge edge, @NotNull List<Edge> path, @NotNull Object value, boolean lossless) {
        return switch (edge) {
            case Edge.ResolvedEdge resolved -> applyMapper(resolved, path, value, lossless);
            case Edge.UnresolvedEdge(Type<Object> from, Type<Object> into) ->
                    convert(value, Type.of(from.container()), Type.of(into.container()), lossless);
        };
    }

    @NotNull
    private ConversionResult<Object> applyMapper(@NotNull Edge.ResolvedEdge edge, @NotNull List<Edge> path, @NotNull Object value, boolean lossless) {
        Mapper.UniMapper<Object, Object> mapper = edge.mapper();
        List<Mapper.UniMapper<Object, Object>> stack = callStack.get();
        if (stack.contains(mapper)) {
            throw new CyclingConversionException(edge.from(), edge.into(), mapper, stack);
        }

        ConversionContext context = new ConversionContext(path, edge);
        stack.add(mapper);
        MappingResult<Object> result = mapper.from(value, new MappingContext<>(edge.from(), edge.into()));

        if (lossless && result instanceof MappingResult.Lossy<?>) {
            return new ConversionResult.Failure<>(NO_LOSSLESS_CONVERSION, "No lossless conversion possible", context);
        }

        stack.remove(mapper);
        return ConversionResult.of(result, MAPPING_FAILED, context);
    }
}
