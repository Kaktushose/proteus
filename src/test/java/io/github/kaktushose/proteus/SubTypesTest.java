package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Flag;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Format;
import io.github.kaktushose.proteus.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubTypesTest {

    private static final Format testFormat = new TestFormat("TestTypeOne");
    private static final Type<DifferentType> TEST_TYPE_ONE = Type.of(testFormat, DifferentType.class);
    private static final Type<BaseType> TEST_TYPE_TWO = Type.of(testFormat, BaseType.class);
    private static final Type<String> TEST_TYPE_THREE = Type.of(new TestFormat("TestTypeTwo"), String.class);
    private static final String INPUT = "INPUT";
    private static final Type<DifferentType> differentType = Type.of(DifferentType.class);
    private static final Type<BaseType> baseType = Type.of(BaseType.class);
    private static final Type<SubType> subType = Type.of(SubType.class);
    private static final Type<BaseInterface> baseInterface = Type.of(BaseInterface.class);
    private static final Type<BaseInterfaceImpl> baseInterfaceImpl = Type.of(BaseInterfaceImpl.class);
    private static final Type<String> string = Type.of(String.class);
    private static final Type<InterfaceAndBaseType> interfaceAndBase = Type.of(InterfaceAndBaseType.class);
    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.builder().defaultMappers().build();
    }

    @Test
    void subTypeConversion_WithSuperTypeAndInterface_ShouldCheckAllPaths() {
        DifferentType instance = new DifferentType();

        proteus.from(baseType)
                .into(string,  Mapper.uni((_, _) -> MappingResult.lossless("")));
        proteus.from(baseInterface)
                .into(differentType, Mapper.uni((_, _) -> MappingResult.lossless(instance)));

        ConversionResult<DifferentType> result = proteus.convert(new InterfaceAndBaseType(), interfaceAndBase, differentType);
        assertEquals(new ConversionResult.Success<>(instance, true), result);
    }

    @Test
    void subTypeConversion_WithInputTypeIsSubTypeOfTarget_ShouldConvert() {
        SubType val = new SubType();
        ConversionResult<BaseType> result = proteus.convert(val, subType, baseType);

        assertEquals(new ConversionResult.Success<>(val, true), result);
    }

    @Test
    void subTypeConversion_WithStrictMode_ShouldReturnNoPathFound() {
        proteus.from(differentType).into(subType, Mapper.uni((_, _) ->
                MappingResult.lossless(new SubType())
        ));
        proteus.from(baseType).into(string, Mapper.uni((source, _) ->
                MappingResult.lossless(source.bValue())
        ), Flag.STRICT_SUB_TYPES);

        ConversionResult<String> result = proteus.convert(new DifferentType(), differentType, string);

        assertEquals(ConversionResult.Failure.ErrorType.NO_PATH_FOUND, ((ConversionResult.Failure<?>) result).errorType());
    }


    @Test
    void strictMode_WithContainerConversion_ShouldReturnNoPathFound() {
        proteus = Proteus.builder().defaultMappers(ProteusBuilder.DefaultMapper.WIDENING_PRIMITIVE).build();
        proteus.from(differentType).into(subType, Mapper.uni((_, _) ->
                        MappingResult.lossless(new SubType())
        ));

        proteus.from(TEST_TYPE_TWO).into(TEST_TYPE_THREE, Mapper.uni((s, _) ->
                MappingResult.lossless(String.valueOf(s))
        ), Flag.STRICT_SUB_TYPES);

        var result = proteus.convert(new DifferentType(), TEST_TYPE_ONE, TEST_TYPE_THREE);

        assertEquals(ConversionResult.Failure.ErrorType.NO_PATH_FOUND, ((ConversionResult.Failure<?>) result).errorType());
    }

    @Test
    void subTypeConversion_WithExplicitMapper_ShouldConvert() {
        proteus.from(differentType).into(subType, Mapper.uni((_, _) ->
                MappingResult.lossless(new SubType())
        ));
        proteus.from(subType).into(baseType, Mapper.uni((source, _) ->
                MappingResult.lossless(source)
        ));
        proteus.from(baseType).into(string, Mapper.uni((source, _) ->
                MappingResult.lossless(source.bValue())
        ));

        ConversionResult<String> result = proteus.convert(new DifferentType(), differentType, string);

        assertEquals(new ConversionResult.Success<>(INPUT, true), result);
    }

    @Test
    void subTypeConversion_WithExplicitRegistration_ShouldConvert() {
        proteus.from(differentType).into(subType, Mapper.uni((_, _) ->
                MappingResult.lossless(new SubType())
        ));
        proteus.from(baseType, subType).into(string, Mapper.uni((source, _) ->
                MappingResult.lossless(source.bValue())
        ));

        ConversionResult<String> result = proteus.convert(new DifferentType(), differentType, string);

        assertEquals(new ConversionResult.Success<>(INPUT, true), result);
    }

    @Test
    void nonStrictRegistration_WithInterfaceAsType_ShouldConvert() {
        proteus.from(differentType).into(baseInterfaceImpl, Mapper.uni((_, _) ->
                MappingResult.lossless(new BaseInterfaceImpl())
        ));
        proteus.from(baseInterface).into(string, Mapper.uni((source, _) ->
                MappingResult.lossless(source.value())
        ));

        ConversionResult<String> result = proteus.convert(new DifferentType(), differentType, string);

        assertEquals(new ConversionResult.Success<>(INPUT, true), result);
    }

    @Test
    void subTypeConversion_WithNonStrictRegistration_ShouldConvert() {
        proteus.from(differentType).into(subType, Mapper.uni((_, _) ->
                MappingResult.lossless(new SubType())
        ));
        proteus.from(baseType).into(string, Mapper.uni((source, _) ->
                MappingResult.lossless(source.bValue())
        ));

        ConversionResult<String> result = proteus.convert(new DifferentType(), differentType, string);

        assertEquals(new ConversionResult.Success<>(INPUT, true), result);
    }

    @Test
    void nonStrictRegistration_WithBaseTypeAsTarget_ShouldConvert() {
        final var resultType = new SubType();
        proteus.from(differentType).into(subType, Mapper.uni((_, _) ->
                MappingResult.lossless(resultType)
        ));
        ConversionResult<BaseType> result = proteus.convert(new DifferentType(), differentType, baseType);

        assertEquals(new ConversionResult.Success<>(resultType, true), result);
    }

    private static class InterfaceAndBaseType extends BaseType implements BaseInterface {}

    private interface BaseInterface {
        default String value() {
            return INPUT;
        }
    }

    private static class BaseInterfaceImpl implements BaseInterface {}

    private static class DifferentType {}

    private static class BaseType {
        String bValue() {
            return INPUT;
        }
    }

    private static class SubType extends BaseType {}

}
