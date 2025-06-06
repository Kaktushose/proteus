package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LosslessConversionTest {

    private static final Type<String> TEST_TYPE_ONE = Type.of(new TestFormat("TestTypeOne"), String.class);
    private static final Type<String> TEST_TYPE_TWO = Type.of(new TestFormat("TestTypeTwo"), String.class);
    private static final String INPUT = "INPUT";
    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.builder().defaultMappers().build();
    }

    @Test
    void losslessConversion_WithLossyMapper_ShouldFail() {
        proteus.from(TEST_TYPE_ONE).into(TEST_TYPE_TWO, Mapper.uni((s, _) ->
                MappingResult.lossy(s)
        ));

        var result = proteus.convert(INPUT, TEST_TYPE_ONE, TEST_TYPE_TWO, true);

        assertEquals(ConversionResult.Failure.class, result.getClass());
        assertEquals(ConversionResult.Failure.ErrorType.NO_LOSSLESS_CONVERSION, ((ConversionResult.Failure<?>) result).errorType());
    }

    @Test
    void losslessConversion_WithLosslessMapper_ShouldWork() {
        proteus.from(TEST_TYPE_ONE).into(TEST_TYPE_TWO, Mapper.uni((s, _) ->
                MappingResult.lossless(s)
        ));

        var result = proteus.convert(INPUT, TEST_TYPE_ONE, TEST_TYPE_TWO, true);

        assertEquals(new ConversionResult.Success<>(INPUT , true), result);
    }

    @Test
    void lossyConversion_WithLossyMapper_ShouldWork() {
        proteus.from(TEST_TYPE_ONE).into(TEST_TYPE_TWO, Mapper.uni((s, _) ->
                MappingResult.lossy(s)
        ));

        var result = proteus.convert(INPUT, TEST_TYPE_ONE, TEST_TYPE_TWO);

        assertEquals(new ConversionResult.Success<>(INPUT , false), result);
    }

    @Test
    void lossyConversion_WithLosslessMapper_ShouldWork() {
        proteus.from(TEST_TYPE_ONE).into(TEST_TYPE_TWO, Mapper.uni((s, _) ->
                MappingResult.lossless(s)
        ));

        var result = proteus.convert(INPUT, TEST_TYPE_ONE, TEST_TYPE_TWO);

        assertEquals(new ConversionResult.Success<>(INPUT , true), result);
    }

}
