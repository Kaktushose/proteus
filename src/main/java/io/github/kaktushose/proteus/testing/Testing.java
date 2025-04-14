package io.github.kaktushose.proteus.testing;

import io.github.kaktushose.proteus.conversion.Graph;
import io.github.kaktushose.proteus.conversion.Result;
import io.github.kaktushose.proteus.conversion.type.Type;
import io.github.kaktushose.proteus.conversion.type.TypeAdapter;

public class Testing {

    public static void main(String[] args) {
        var graph = new Graph();
        var string = new Type.Universal<>(String.class);
        var integer = new Type.Universal<>(Integer.class);
        var longType = new Type.Universal<>(Long.class);
        var doubleType = new Type.Universal<>(Double.class);
        graph.register(new TypeAdapter<>(string, integer, (source, context) -> {
            try {
                return Result.success(Integer.valueOf(source));
            } catch (Exception e) {
                return Result.failure(e.toString());
            }
        }));
        graph.register(new TypeAdapter<>(integer, longType, (source, context) -> Result.success((long) source)));
        graph.register(new TypeAdapter<>(longType, doubleType, (source, context) -> Result.success((double) source)));

        System.out.println(graph.convert("10s", string, doubleType));
    }
}
