package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LosslessConversionTest {

    private static final Type<String> TEST_TYPE_ONE = Type.of(new TestFormat("TestTypeOne"), String.class);
    private static final Type<String> TEST_TYPE_TWO = Type.of(new TestFormat("TestTypeTwo"), String.class);
    private static Proteus proteus = new Proteus();

    @BeforeEach
    void init() {
        proteus = new Proteus();
    }

    @Test
    void losslessConversion_WithLossyMapper_ShouldFail() {
        proteus.graph().register(new TypeAdapter<>(TEST_TYPE_ONE, TEST_TYPE_TWO, Mapper.UniMapper.lossy((s, c) ->
                MappingResult.success(s)
        )));

        var result = proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO, true);

        assertEquals(ConversionResult.Failure.class, result.getClass());
        assertEquals(ConversionResult.Failure.ErrorType.NO_LOSSLESS_CONVERSION, ((ConversionResult.Failure<?>) result).errorType());
    }

    @Test
    void lossyConversion_WithLossyMapper_ShouldWork() {
        proteus.graph().register(new TypeAdapter<>(TEST_TYPE_ONE, TEST_TYPE_TWO, Mapper.UniMapper.lossless((s, c) ->
                MappingResult.success(s)
        )));

        final var input = "INPUT";
        var result = proteus.convert(input, TEST_TYPE_ONE, TEST_TYPE_TWO);

        assertEquals(ConversionResult.Success.class, result.getClass());
        assertEquals(input, ((ConversionResult.Success<String>) result).value());
    }
}
