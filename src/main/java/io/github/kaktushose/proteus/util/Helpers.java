package io.github.kaktushose.proteus.util;

import io.github.kaktushose.proteus.conversion.Result;
import io.github.kaktushose.proteus.graph.Edge;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.internal.Java;
import io.github.kaktushose.proteus.type.internal.Specific;
import io.github.kaktushose.proteus.type.internal.Universal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Helpers {

    private static final Map<Class<?>, List<Class<?>>> allowedRoutes = Map.of(
            Universal.class, List.of(Universal.class),
            Java.class, List.of(Java.class, Specific.class),
            Specific.class, List.of(Specific.class, Java.class)
    );

    public static boolean invalidRoute(@NotNull Class<?> from, @NotNull Class<?> into) {
        return !allowedRoutes.get(from).contains(into);
    }

    @NotNull
    public static String formatError(@NotNull List<Edge> path, @NotNull Result.Failure<?> failure, @Nullable Edge step) {
        Type<?> from;
        Type<?> into;
        var index = path.indexOf(step) - 1;
        step = path.get(Math.max(index, 0));
        if (step == null) {
            from = path.getLast().from();
            into = path.getLast().into();
        } else {
            from = step.from();
            into = step.into();
        }
        StringBuilder error = new StringBuilder();
        error.append("Failed to convert from '%s' to '%s'.\n".formatted(path.getFirst().from(), path.getLast().into()))
                .append("Type adapting failed for step '%s' -> '%s'! Reason: '%s'\n".formatted(from, into, failure.message()))
                .append("Path: \n");

        error.append("\n");
        for (Edge edge : path) {
            if (edge.from().equals(from) || edge.from().equals(into)) {
                error.append("  -> %s".formatted(edge.from()));
            } else {
                error.append("     ").append(edge.from());
            }
            error.append("\n");
        }
        var last = path.getLast();
        if (last != null) {
            if (last.into().equals(from) || last.into().equals(into)) {
                error.append("  -> %s".formatted(last.into()));
            } else {
                error.append("     ").append(last.into());
            }
        }
        return error.toString();
    }
}
