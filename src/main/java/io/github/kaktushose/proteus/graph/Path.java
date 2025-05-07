package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.conversion.Mapper;
import io.github.kaktushose.proteus.type.Format;
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
        List<Edge> newEdges = new ArrayList<>(edges);
        if (mapper != null) {
            newEdges.add(new Edge.ResolvedEdge(head, (Type<Object>) intermediate, (Mapper.UniMapper<Object, Object>) mapper));
            return new Path(newEdges, (Type<Object>) intermediate);
        }
        if (!head.equalsFormat(intermediate)) {
            throw new IllegalArgumentException("Illegal edge for two types with different format. Please report this error to the devs of proteus!");
        }
        if (head.format() instanceof Format.None || intermediate.format() instanceof Format.None) {
            throw new UnsupportedOperationException("Mapper cannot be null for types without a format. Please report this error to the devs of proteus!");
        }
        newEdges.add(new Edge.UnresolvedEdge(head, (Type<Object>) intermediate));
        return new Path(newEdges, (Type<Object>) intermediate);
    }

    @NotNull
    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }
}
