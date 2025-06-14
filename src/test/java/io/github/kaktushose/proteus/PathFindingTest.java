package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Format;
import io.github.kaktushose.proteus.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathFindingTest {

    private static final Type<String> TEST_TYPE_ONE = Type.of(new TestFormat("TestTypeOne"), String.class);
    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.builder().defaultMappers().build();
    }


    @Test
    void conversion_withTargetEqualsSource_ShouldReturnSourceValue() {
        final var input = "INPUT";

        var result = proteus.convert(input, TEST_TYPE_ONE, TEST_TYPE_ONE);

        assertEquals(ConversionResult.Success.class, result.getClass());
        assertEquals(input, ((ConversionResult.Success<String>) result).value());
    }

    @Test
    void existsPath_withNoExistingPath_ShouldReturnFalse() {
        assertFalse(proteus.existsPath(Type.of(FirstType.class), Type.of(SecondType.class)));
    }

    @Test
    void existsPath_withExistingPath_ShouldReturnTrue() {
        proteus.register(Type.of(FirstType.class), Type.of(SecondType.class), Mapper.uni((_, _) -> MappingResult.failure("")));

        assertTrue(proteus.existsPath(Type.of(FirstType.class), Type.of(SecondType.class)));
    }

    @Test
    void existsPath_withSourceAsTaget_ShouldReturnTrue() {
        assertTrue(proteus.existsPath(Type.of(FirstType.class), Type.of(FirstType.class)));
    }

    @Test
    void existsPath_withContainerConversion_ShouldReturnTrue() {
        final Format firstFormat = new TestFormat("firstFormat");
        final Format secondFormat = new TestFormat("secondFormat");

        proteus.register(
                Type.of(firstFormat, FirstType.class),
                Type.of(secondFormat, SecondType.class),
                Mapper.uni((_, _) -> MappingResult.failure(""))
        );
        proteus.register(
                Type.of(FirstType.class),
                Type.of(SecondType.class),
                Mapper.uni((_, _) -> MappingResult.failure(""))
        );

        assertTrue(proteus.existsPath(Type.of(secondFormat, FirstType.class), Type.of(secondFormat, SecondType.class)));
    }

    @Test
    void existsPath_withNoPathForContainerConversion_ShouldReturnFalse() {
        final Format firstFormat = new TestFormat("firstFormat");
        final Format secondFormat = new TestFormat("secondFormat");

        proteus.register(
                Type.of(firstFormat, FirstType.class),
                Type.of(secondFormat, SecondType.class),
                Mapper.uni((_, _) -> MappingResult.failure(""))
        );

        assertFalse(proteus.existsPath(Type.of(secondFormat, FirstType.class), Type.of(secondFormat, SecondType.class)));
    }

    private record FirstType() {}

    private record SecondType() {}
}
