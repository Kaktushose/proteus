package io.github.kaktushose.proteus.testing;

import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.conversion.Result;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeAdapter;
import io.github.kaktushose.proteus.type.internal.Specific;

public class Testing {

    public static void main(String[] args) {
        var proteus = new Proteus();
        var graph = proteus.graph();

        var S1String = Type.specific("S1", "", "", Integer.class);
        var S1Double = Type.specific("S1", "", "", Double.class);
        var S2String = Type.specific("S2", "", "", String.class);

        graph.register(new TypeAdapter<>(S1Double, S2String, ((source, context) -> Result.success(String.valueOf(source)))));

        System.out.println(proteus.convert(1, S1String, S2String));
    }
}
