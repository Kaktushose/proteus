package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.conversion.Mapper;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public record Path(@NotNull List<Edge> edges, @NotNull Type<Object> head) {

    public Path(@NotNull Type<?> head) {
        this(new ArrayList<>(), (Type<Object>) head);
    }

    public Path addEdge(@NotNull Type<?> intermediate, @Nullable Mapper.UniMapper<?, ?> mapper) {
        var newEdges = new ArrayList<>(edges);
        if (mapper != null) {
            newEdges.add(new Edge.ResolvedEdge(head, (Type<Object>) intermediate, (Mapper.UniMapper<Object, Object>) mapper));
            return new Path(newEdges, (Type<Object>) intermediate);
        }

        if (head instanceof Type.Specific<Object> from && intermediate instanceof Type.Specific<?> into) {
            if (!from.equalsIgnoreContainer(into)) {
                throw new IllegalArgumentException("Illegal edge for two specific types with different format. Please report this error to the devs of proteus!");
            }
        } else {
            throw new UnsupportedOperationException("Mapper cannot be null for non-specific types. Please report this error to the devs of proteus!");
        }

        newEdges.add(new Edge.UnresolvedEdge(from, (Type.Specific<Object>) into));
        return new Path(newEdges, (Type<Object>) intermediate);
    }

    @NotNull
    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }
}
