package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.conversion.Mapper.UniMapper;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.Type.Specific;
import org.jetbrains.annotations.NotNull;

sealed public interface Edge {

    @NotNull
    Type<Object> from();

    @NotNull
    Type<Object> into();

    record UnresolvedEdge(@NotNull Specific<Object> from, @NotNull Specific<Object> into) implements Edge {}

    record ResolvedEdge(@NotNull Type<Object> from, @NotNull Type<Object> into, @NotNull UniMapper<Object, Object> mapper) implements Edge {}
}
