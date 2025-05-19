package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamicTypeTest {
    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.create();
    }

    @Test
    void conversationWithArbitraryType() {
        proteus.map(Type.of(String.class)).to(Type.of(Text.class), Mapper.uni((string, _) -> MappingResult.lossless(new Text(string))));

        ConversionResult<Text> result = proteus.convert("ABC", Type.dynamic("ABC"), Type.of(Text.class));

        assertEquals(new ConversionResult.Success<>(new Text("ABC"), true), result);
    }

    record Text(String text) {}
}
