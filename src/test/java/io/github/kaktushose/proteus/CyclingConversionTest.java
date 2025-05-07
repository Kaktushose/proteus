package io.github.kaktushose.proteus;


import io.github.kaktushose.proteus.conversion.CyclingConversionException;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;
import io.github.kaktushose.proteus.type.Type;
import io.github.kaktushose.proteus.type.TypeAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CyclingConversionTest {

    private static final Type<String> TEST_TYPE_ONE = Type.of(new TestFormat("TestTypeOne"), String.class);
    private static final Type<String> TEST_TYPE_TWO = Type.of(new TestFormat("TestTypeTwo"), String.class);
    private static final Type<String> TEST_TYPE_THREE = Type.of(new TestFormat("TestTypeThree"), String.class);
    private static Proteus proteus = new Proteus();

    @BeforeEach
    void init() {
        proteus = new Proteus();
    }

    @Test
    void conversion_withSelfCall_ShouldThrow() {
        proteus.graph().register(new TypeAdapter<>(TEST_TYPE_ONE, TEST_TYPE_TWO, Mapper.UniMapper.lossless((s, c) -> {
            proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO);
            return MappingResult.success(s);
        })));

        assertThrows(CyclingConversionException.class, () -> proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO));
    }

    @Test
    void conversion_withCyclingCall_ShouldThrow() {
        proteus.graph().register(new TypeAdapter<>(TEST_TYPE_ONE, TEST_TYPE_TWO, Mapper.UniMapper.lossless((s, c) -> {
            proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO);
            return MappingResult.success(s);
        })));

        proteus.graph().register(new TypeAdapter<>(TEST_TYPE_TWO, TEST_TYPE_ONE, Mapper.UniMapper.lossless((s, c) -> {
            proteus.convert("", TEST_TYPE_TWO, TEST_TYPE_ONE);
            return MappingResult.success(s);
        })));

        assertThrows(CyclingConversionException.class, () -> proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO));
    }

    @Test
    void conversion_withNestedCall_ShouldWork() {
        proteus.graph().register(new TypeAdapter<>(TEST_TYPE_ONE, TEST_TYPE_TWO, Mapper.UniMapper.lossless((s, c) ->
                MappingResult.success(s)
        )));

        proteus.graph().register(new TypeAdapter<>(TEST_TYPE_ONE, TEST_TYPE_THREE, Mapper.UniMapper.lossless((s, c) ->
                MappingResult.of(proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_TWO))))
        );

        assertDoesNotThrow(() -> proteus.convert("", TEST_TYPE_ONE, TEST_TYPE_THREE));
    }
}
