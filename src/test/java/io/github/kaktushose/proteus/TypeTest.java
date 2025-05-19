package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeTest {
    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.create();
    }

    @Test
    void conversion_withArbitraryType_ShouldDynamicallyResolveType() {
        proteus.map(Type.of(String.class)).to(Type.of(Text.class), Mapper.uni((string, _) -> MappingResult.lossless(new Text(string))));

        ConversionResult<Text> result = proteus.convert("ABC", Type.dynamic("ABC"), Type.of(Text.class));

        assertEquals(new ConversionResult.Success<>(new Text("ABC"), true), result);
    }

    record Text(String text) {}

    @Test
    void conversion_withPrimitive_ShouldMatchWrapped() {
        proteus.map(Type.of(long.class)).to(Type.of(Integer.class), Mapper.uni((source, _) -> MappingResult.lossless(source.intValue())));

        ConversionResult<Integer> result = proteus.convert(10L, Type.of(Long.class), Type.of(int.class));

        assertEquals(new ConversionResult.Success<>(10, true), result);
    }

    @Test
    void typeDeclaration_withPrimitives_ShouldReturnWrapperType() {
        assertEquals(Type.of(int.class), Type.of(Integer.class));
        assertEquals(Type.of(new TestFormat(""), int.class), Type.of(new TestFormat(""), Integer.class));
        assertEquals(Type.dynamic(1), Type.of(Integer.class));
    }
}
