package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.MappingResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultTest {

    private static final Object SUCCESS = "SUCCESS";

    @Test
    void conversionResult_ToMappingResult_ShouldPreserveState() {
        var conversionResult = new ConversionResult.Success<>(SUCCESS, true);

        var mappingResult = MappingResult.of(conversionResult);

        assertEquals(new MappingResult.Lossless<>(SUCCESS), mappingResult);
    }
}
