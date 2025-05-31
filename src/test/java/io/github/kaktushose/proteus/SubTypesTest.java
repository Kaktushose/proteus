package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubTypesTest {

    private static final String INPUT = "INPUT";
    private static final Type<DifferentType> different = Type.of(DifferentType.class);
    private static final Type<BaseType> base = Type.of(BaseType.class);
    private static final Type<SubType> sub = Type.of(SubType.class);
    private static final Type<String> string = Type.of(String.class);
    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.create();
    }

    @Test
    void subTypeConversion_WithNoExplicitMapper_ShouldReturnNoPathFound() {
        proteus.map(different).to(sub, Mapper.uni((_, _) ->
                MappingResult.lossless(new SubType())
        ));
        proteus.map(base).to(string, Mapper.uni((source, _) ->
                MappingResult.lossless(source.value())
        ));

        ConversionResult<String> result = proteus.convert(new DifferentType(), different, string);

        assertEquals(ConversionResult.Failure.ErrorType.NO_PATH_FOUND, ((ConversionResult.Failure<?>) result).errorType());
    }

    @Test
    void subTypeConversion_WithExplicitMapper_ShouldConvert() {
        proteus.map(different).to(sub, Mapper.uni((_, _) ->
                MappingResult.lossless(new SubType())
        ));
        proteus.map(sub).to(base, Mapper.uni((source, _) ->
                MappingResult.lossless(source)
        ));
        proteus.map(base).to(string, Mapper.uni((source, _) ->
                MappingResult.lossless(source.value())
        ));

        ConversionResult<String> result = proteus.convert(new DifferentType(), different, string);

        assertEquals(new ConversionResult.Success<>(INPUT, true), result);
    }

    @Test
    void subTypeConversion_WithExplicitRegistration_ShouldConvert() {
        proteus.map(different).to(sub, Mapper.uni((_, _) ->
                MappingResult.lossless(new SubType())
        ));
        proteus.map(base, sub).to(string, Mapper.uni((source, _) ->
                MappingResult.lossless(source.value())
        ));

        ConversionResult<String> result = proteus.convert(new DifferentType(), different, string);

        assertEquals(new ConversionResult.Success<>(INPUT, true), result);
    }

    @Test
    void subTypeConversion_WithGenericRegistration_ShouldConvert() {
        proteus.map(different).to(sub, Mapper.uni((_, _) ->
                MappingResult.lossless(new SubType())
        ));
        proteus.map(base).to(string, Mapper.uni((source, _) ->
                MappingResult.lossless(source.value())
        ));

        ConversionResult<String> result = proteus.convert(new DifferentType(), different, string);

        assertEquals(new ConversionResult.Success<>(INPUT, true), result);
    }

    private static class DifferentType {}

    private static class BaseType {
        String value() {
            return INPUT;
        }
    }

    private static class SubType extends BaseType {}

}
