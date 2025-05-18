package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathFindingTest {

    private static final Type<String> TEST_TYPE_ONE = Type.of(new TestFormat("TestTypeOne"), String.class);
    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.create();
    }


    @Test
    void conversion_withTargetEqualsSource_ShouldReturnSourceValue() {
        final var input = "INPUT";

        var result = proteus.convert(input, TEST_TYPE_ONE, TEST_TYPE_ONE);

        assertEquals(ConversionResult.Success.class, result.getClass());
        assertEquals(input, ((ConversionResult.Success<String>) result).value());
    }

}
