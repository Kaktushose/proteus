package io.github.kaktushose.proteus;


import io.github.kaktushose.proteus.conversion.CyclingConversionException;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CyclingConversionTest {

    private static final Type<String> TEST_TYPE_ONE = Type.of(new TestFormat("TestTypeOne"), String.class);
    private static final Type<String> TEST_TYPE_TWO = Type.of(new TestFormat("TestTypeTwo"), String.class);
    private static final Type<String> TEST_TYPE_THREE = Type.of(new TestFormat("TestTypeThree"), String.class);
    private static Proteus proteus;

    @BeforeEach
    void init() {
        proteus = Proteus.create();
    }

    @Test
    void conversion_withSelfCall_ShouldThrow() {
        proteus.from(TEST_TYPE_ONE).into(TEST_TYPE_TWO, Mapper.uni((s, _) -> {
            proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO);
            return MappingResult.lossless(s);
        }));

        assertThrows(CyclingConversionException.class, () -> proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO));
    }

    @Test
    void conversion_withCyclingCall_ShouldThrow() {
        proteus.from(TEST_TYPE_ONE).into(TEST_TYPE_TWO, Mapper.uni((s, _) -> {
            proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO);
            return MappingResult.lossless(s);
        }));
        proteus.from(TEST_TYPE_TWO).into(TEST_TYPE_ONE, Mapper.uni((s, _) -> {
            proteus.convert("", TEST_TYPE_TWO, TEST_TYPE_ONE);
            return MappingResult.lossless(s);
        }));

        assertThrows(CyclingConversionException.class, () -> proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO));
    }

    @Test
    void conversion_withNestedCall_ShouldWork() {
        proteus.from(TEST_TYPE_ONE).into(TEST_TYPE_TWO, Mapper.uni((s, _) ->
                MappingResult.lossless(s)
        ));
        proteus.from(TEST_TYPE_ONE).into(TEST_TYPE_THREE, Mapper.uni((s, _) ->
                MappingResult.of(proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO))
        ));

        assertDoesNotThrow(() -> proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_THREE));
    }
}
