package io.github.kaktushose.proteus.type;

/// Implementations of [Format] are used to give [Type]s additional information.
public interface Format {

    /// Whether this format is equal to (and thus compatible) to the given [Format].
    ///
    /// @param other the [Format] to compare against
    /// @return `true` if this format is compatible with the given [Format]
    boolean equals(Format other);

    /// Creates a new [Format] of type [None].
    static None none() {
        return new None();
    }

    /// An implementation of [Format] to indicate that *no* format is present. [Format#equals(Format)] will *always*
    /// return `false`.
    record None() implements Format {

        /// Whether this format is equal to (and thus compatible) to the given [Format], which is always `false` for the
        /// [None] format.
        ///
        /// @param other the [Format] to compare against
        /// @return `false`
        @Override
        public boolean equals(Format other) {
            return false;
        }
    }
}
