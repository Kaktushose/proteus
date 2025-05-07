package io.github.kaktushose.proteus.testing;

import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.conversion.Mapper;
import io.github.kaktushose.proteus.conversion.Result;
import io.github.kaktushose.proteus.type.Format;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeAdapter;

import static io.github.kaktushose.proteus.testing.Testing.TestingFormat.format;

public class Testing {

    public static void main(String[] args) {
        var proteus = new Proteus();
        var graph = proteus.graph();

        var S1Integer = Type.of(format("S1"), Integer.class);
        var S1Double = Type.of(format("S1"), Double.class);
        var S2String = Type.of(format("S2"), String.class);

        graph.register(new TypeAdapter<>(S1Double, S2String, Mapper.UniMapper.lossy((source, context) -> Result.success(String.valueOf(source)))));

        System.out.println(proteus.convert(1, S1Integer, S2String, false));
    }

    public record TestingFormat(String format) implements Format {

        public static TestingFormat format(String format) {
            return new TestingFormat(format);
        }

        @Override
        public boolean equals(Format format) {
            if (format instanceof TestingFormat(String other)) {
                return this.format.equals(other);
            }
            return false;
        }
    }
}
