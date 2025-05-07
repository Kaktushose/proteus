package io.github.kaktushose.proteus.conversion;

import io.github.kaktushose.proteus.graph.Edge;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface ConversionResult<T> {

    record Success<T>(@NotNull T value) implements ConversionResult<T> {}

    record Failure<T>(@NotNull ErrorType errorType, @NotNull String message, @Nullable Mapper.MappingContext context) implements ConversionResult<T> {

        @NotNull
        public String formatMessage() {
            if (context == null) {
                return message;
            }
            List<Edge> path = context.path();
            Edge.ResolvedEdge step = context.step();
            Type<?> from = step.from();
            Type<?> into = step.into();
            int index = path.indexOf(step);
            StringBuilder error = new StringBuilder();
            error.append("Failed to convert from '%s' to '%s'\n".formatted(context.from(), context.into()))
                    .append("Reason:\n     %s(message=%s)\n".formatted(errorType, message))
                    .append("Step:\n     '%s' -> '%s':\n".formatted(from, into))
                    .append("Path:\n");
            for (int i = 0; i < path.size(); i++) {
                var edge = path.get(i);
                if (i == index) {
                    error.append("  -> %s".formatted(edge.from())).append("\n").append("  -> %s".formatted(edge.into()));
                } else {
                    error.append("     ").append(edge.from()).append("\n").append("     ").append(edge.into());
                }
                error.append("\n");
            }
            return error.toString();
        }

        public enum ErrorType {
            NO_PATH_FOUND,
            MAPPING_FAILED,
            NO_LOSSLESS_CONVERSION
        }
    }
}
