package io.github.kaktushose.proteus.graph;

import io.github.kaktushose.proteus.mapping.Mapper.UniMapper;
import io.github.kaktushose.proteus.type.Format;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeReference;
import org.jetbrains.annotations.NotNull;

/// An edge of the mapper graph. More formally, this represents the connection between two vertices `u` and `v` of the
/// graph, referred to as `from` and `into`.
///
/// @param from   the [Type] representing the starting node of this edge
/// @param into   the [Type] representing the ending node of this edge
/// @param mapper the corresponding [UniMapper] to map from `from` into `into`
public record Edge(@NotNull Type<Object> from, @NotNull Type<Object> into, @NotNull UniMapper<Object, Object> mapper) {
}
