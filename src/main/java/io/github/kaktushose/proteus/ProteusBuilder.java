package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.graph.Graph;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

/// Builder for [Proteus] instances.
public final class ProteusBuilder {

    private int cacheSize;
    private EnumSet<DefaultMapper> defaultMappers;
    private ConflictStrategy conflictStrategy;

    /// Creates a new [ProteusBuilder].
    ProteusBuilder() {
        cacheSize = 1000;
        defaultMappers = EnumSet.copyOf(List.of(DefaultMapper.values()));
        conflictStrategy = ConflictStrategy.FAIL;
    }

    /// The cache size to use for the underlying LRU-Cache used for caching conversion paths. The default value is `1000`.
    ///
    /// @param cacheSize cache size to use for the underlying LRU-Cache
    /// @return this instance for fluent interface
    @NotNull
    public ProteusBuilder cacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    /// Whether to register default mappers as described by [DefaultMapper]. These default mappers are lossless and
    /// follow the widening and narrowing primitive conversion of the Java Language Specification.
    ///
    /// @implNote By default, all [DefaultMapper]s will be registered. Can be disabled by calling this method with zero
    /// arguments.
    ///
    /// @param defaultMappers the [DefaultMapper]s to register
    /// @return this instance for fluent interface
    /// @see <a href="https://docs.oracle.com/javase/specs/jls/se10/html/jls-5.html#jls-5.1.2">Java Language Specification</a>
    @NotNull
    public ProteusBuilder defaultMappers(DefaultMapper... defaultMappers) {
        this.defaultMappers = defaultMappers.length == 0 ? EnumSet.noneOf(DefaultMapper.class) : EnumSet.copyOf(List.of(defaultMappers));
        return this;
    }

    /// The [ConflictStrategy] to use if a duplicate path registration happens. Although, each [Type] can have `n`
    /// neighbours, but each path can only exist once.
    ///
    /// @param conflictStrategy the [ConflictStrategy] to use
    /// @return this instance for fluent interface
    @NotNull
    public ProteusBuilder conflictStrategy(@NotNull ConflictStrategy conflictStrategy) {
        this.conflictStrategy = conflictStrategy;
        return this;
    }

    /// Builds the [Proteus] instance.
    ///
    /// @return the [Proteus] instance
    @NotNull
    public Proteus build() {
        Graph graph = new Graph(cacheSize);
        Proteus proteus = new Proteus(graph, conflictStrategy);

        for (DefaultMapper defaultMapper : defaultMappers) {
            switch (defaultMapper) {
                case WIDENING_PRIMITIVE -> LosslessDefaultMappers.wideningPrimitives(proteus);
                case NARROWING_PRIMITIVE -> LosslessDefaultMappers.narrowingPrimitives(proteus);
                case STRING -> LosslessDefaultMappers.string(proteus);
                case BIG_DECIMAL -> LosslessDefaultMappers.bigDecimal(proteus);
            }
        }

        return proteus;
    }

    /// The [ConflictStrategy] to use if a duplicate path registration happens.
    public enum ConflictStrategy {
        /// Will throw an [IllegalArgumentException] if a duplicate path registration happens.
        FAIL,
        /// Will silently fail if a duplicate path registration happens.
        IGNORE,
        /// Will override the existing path with the newly registered one.
        OVERRIDE
    }

    /// The [DefaultMapper]s to register.
    public enum DefaultMapper {
        /// Registers default mappers for widening primitive conversion.
        WIDENING_PRIMITIVE,
        /// Registers default mappers for narrowing primitive conversion.
        NARROWING_PRIMITIVE,
        /// Registers bidirectional mappers for `char[]`, [String], [StringBuffer] and [StringBuilder].
        STRING,
        /// Registers a default mapper for [Double] to [BigDecimal].
        BIG_DECIMAL
    }
}
