package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.mapping.Mapper.UniMapper;
import io.github.kaktushose.proteus.type.Format;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// Representation of an unfinished path.
///
/// @param edges a [List] of all previous [Edge]s
/// @param head  the [Type] that is currently the result of this path. In other words, this is the [Type] that the last
///              [Edge] in the list of edges maps to and thus the [Type] that the next [Edge] has to start with.
@ApiStatus.Internal
@SuppressWarnings("unchecked")
record Path(@NotNull List<Edge> edges, @NotNull Type<Object> head) {

    public Path {
        edges = Collections.unmodifiableList(edges);
    }

    /// Constructs a new empty path with the head pointed to the given type.
    ///
    /// @param head the [Type] that the next [Edge] registered will start with
    public Path(@NotNull Type<?> head) {
        this(new ArrayList<>(), (Type<Object>) head);
    }

    /// Adds a new edge to this path. This will create a copy of this path, the underlying list of edges of this path
    /// will not be modified.
    ///
    /// @param intermediate the [Type] that this edge maps into. Will become the new head
    /// @param vertex       the [UniMapper] that maps from the `head` of this path to the given `intermediate` [Type].
    ///                     Can be null, if `head` and `intermediate` share the same [Format].
    /// @return a copy of this path with the given edge added to it
    /// @throws IllegalArgumentException      if the `vertex` is null and `head` and `intermediate` don't share the same [Format]
    public Path addEdge(@NotNull Type<?> intermediate, @Nullable Graph.Vertex vertex) {
        List<Edge> newEdges = new ArrayList<>(edges);
        if (vertex != null) {
            newEdges.add(new Edge(head, (Type<Object>) intermediate, vertex.mapper()));
            return new Path(newEdges, (Type<Object>) intermediate);
        }
        if (!head.format().equals(intermediate.format())) {
            throw new IllegalArgumentException(
                    "Illegal edge for two types with different format ('%s' and '%s'). Please report this error to the devs of proteus!"
                            .formatted(head.format(), intermediate.format())
            );
        }
        return new Path(newEdges, (Type<Object>) intermediate);
    }

    /// Returns a copy of this path with the given head as its new head.
    ///
    /// @param head the [Type] to use as a new head
    /// @return a copy of this path with the updated head
    public Path withHead(@NotNull Type<Object> head) {
        return new Path(edges, head);
    }
}
