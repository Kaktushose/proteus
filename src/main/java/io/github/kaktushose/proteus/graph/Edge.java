package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.mapping.Mapper.UniMapper;
import io.github.kaktushose.proteus.type.Format;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeReference;
import org.jetbrains.annotations.NotNull;

/// An edge of the mapper graph. More formally, this represents the connection between two vertices `u` and `v` of the
/// graph, referred to as `from` and `into`.
sealed public interface Edge {

    /// The starting node of this edge.
    ///
    /// @return the [Type] representing the starting node of this edge
    @NotNull
    Type<Object> from();

    /// The ending node of this edge.
    ///
    /// @return the [Type] representing the ending node of this edge
    @NotNull
    Type<Object> into();

    /// Represents an [Edge] where the connection is theoretically possible but no mapper has been found (yet). This
    /// might the case if the two [Type]s share the same [Format] and only differ in their [TypeReference].
    ///
    /// @param from the [Type] representing the starting node of this edge
    /// @param into the [Type] representing the ending node of this edge
    record UnresolvedEdge(@NotNull Type<Object> from, @NotNull Type<Object> into) implements Edge {}

    /// Represents an [Edge] with an [UniMapper] already associated with it.
    ///
    /// @param from   the [Type] representing the starting node of this edge
    /// @param into   the [Type] representing the ending node of this edge
    /// @param mapper the corresponding [UniMapper] to map from `from` into `into`
    record ResolvedEdge(@NotNull Type<Object> from, @NotNull Type<Object> into, @NotNull UniMapper<Object, Object> mapper) implements Edge {}
}
