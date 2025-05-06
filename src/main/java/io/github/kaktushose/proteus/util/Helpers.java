package io.github.kaktushose.proteus.util;

import io.github.kaktushose.proteus.conversion.Result;
import io.github.kaktushose.proteus.graph.Edge;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Helpers {

    @NotNull
    public static String formatError(@NotNull List<Edge> path, @NotNull Result.Failure<?> failure, @NotNull Edge step) {
        Type<?> from = step.from();
        Type<?> into = step.into();
        int index = path.indexOf(step);
        StringBuilder error = new StringBuilder();
        error.append("Failed to convert from '%s' to '%s'.\n".formatted(path.getFirst().from(), path.getLast().into()))
                .append("Type adapting failed for step '%s' -> '%s'! Reason: '%s'\n".formatted(from, into, failure.message()))
                .append("Path: \n");

        error.append("\n");
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
}
