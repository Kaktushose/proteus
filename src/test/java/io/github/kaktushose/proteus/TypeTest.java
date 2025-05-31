package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TypeTest {
    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.builder().defaultMappers(false).build();
    }

    @Test
    void conversion_withArbitraryType_ShouldDynamicallyResolveType() {
        proteus.from(Type.of(String.class)).into(Type.of(Text.class), Mapper.uni((string, _) -> MappingResult.lossless(new Text(string))));

        ConversionResult<Text> result = proteus.convert("ABC", Type.dynamic("ABC"), Type.of(Text.class));

        assertEquals(new ConversionResult.Success<>(new Text("ABC"), true), result);
    }

    record Text(String text) {}

    @Test
    void conversion_withPrimitive_ShouldMatchWrapped() {
        proteus.from(Type.of(long.class)).into(Type.of(Integer.class), Mapper.uni((source, _) -> MappingResult.lossless(source.intValue())));

        ConversionResult<Integer> result = proteus.convert(10L, Type.of(Long.class), Type.of(int.class));

        assertEquals(new ConversionResult.Success<>(10, true), result);
    }

    @Test
    void typeDeclaration_withPrimitives_ShouldReturnWrapperType() {
        assertEquals(Type.of(int.class), Type.of(Integer.class));
        assertEquals(Type.of(new TestFormat(""), int.class), Type.of(new TestFormat(""), Integer.class));
        assertEquals(Type.dynamic(1), Type.of(Integer.class));
    }

    @Test
    void fromMappingAction_andIntoMappingAction_ShouldBeIdentical() {
        final var firstType = Type.of(new TestFormat("first"), Integer.class);
        final var secondType = Type.of(new TestFormat("second"), String.class);
        final var input = 10;

        proteus = Proteus.builder().defaultMappers(false).build();
        proteus.from(firstType).into(secondType, Mapper.uni((i, _) -> MappingResult.lossless(i + "")));
        final var firstResult = proteus.convert(input, firstType, secondType);

        proteus = Proteus.builder().defaultMappers(false).build();
        proteus.into(secondType).from(firstType, Mapper.uni((i, _) -> MappingResult.lossless(i + "")));
        final var secondResult = proteus.convert(input, firstType, secondType);

        assertEquals(firstResult, secondResult);
    }
}
