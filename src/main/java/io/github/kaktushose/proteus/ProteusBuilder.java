package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.graph.Graph;
import io.github.kaktushose.proteus.type.Type;
import org.jetbrains.annotations.NotNull;

/// Builder for [Proteus] instances.
public final class ProteusBuilder {

    private int cacheSize;
    private boolean defaultMappers;
    private ConflictStrategy conflictStrategy;

    /// Creates a new [ProteusBuilder].
    ProteusBuilder() {
        cacheSize = 1000;
        defaultMappers = true;
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

    /// Whether to register default mapper for primitive types. These default mappers are lossless and follow the
    /// widening primitive conversion of the Java Language Specification. Additionally, there are bidirectional mappers
    /// for `char[]`, [String], [StringBuffer] and [StringBuilder].
    ///
    /// @param defaultMappers `true` i to register default mappers
    /// @return this instance for fluent interface
    /// @see <a href="https://docs.oracle.com/javase/specs/jls/se10/html/jls-5.html#jls-5.1.2">Java Language Specification</a>
    @NotNull
    public ProteusBuilder defaultMappers(boolean defaultMappers) {
        this.defaultMappers = defaultMappers;
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

        if (defaultMappers) {
            LosslessDefaultMappers.registerMappers(proteus);
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
}
