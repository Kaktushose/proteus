package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Format;
import io.github.kaktushose.proteus.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ContainerConversionTest {

    private static final Format testFormat = new TestFormat("TestTypeOne");
    private static final Type<Integer> TEST_TYPE_ONE = Type.of(testFormat, Integer.class);
    private static final Type<Long> TEST_TYPE_TWO = Type.of(testFormat, Long.class);
    private static final Type<String> TEST_TYPE_THREE = Type.of(new TestFormat("TestTypeTwo"), String.class);

    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.builder().defaultMappers(false).build();
    }

    @Test
    void conversion_withImplicitContainerPath_ShouldWork() {
        proteus = Proteus.builder().defaultMappers(true).build();
        proteus.from(TEST_TYPE_TWO).into(TEST_TYPE_THREE, Mapper.uni((s, _) ->
                MappingResult.lossless(String.valueOf(s)))
        );

        final var input = 10;
        var result = proteus.convert(input, TEST_TYPE_ONE, TEST_TYPE_THREE);

        assertEquals(ConversionResult.Success.class, result.getClass());
        assertEquals(String.valueOf(input), ((ConversionResult.Success<String>) result).value());
    }
}
