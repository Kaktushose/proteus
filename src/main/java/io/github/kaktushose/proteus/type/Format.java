package io.github.kaktushose.proteus.type;

/// Implementations of [Format] are used to give [Type]s additional information.
public interface Format {

    /// An implementation of [Format] to indicate that *no* format is present. [Format#equals(Format)] will return `true`
    /// if the other format is also of [Format#NONE].
    Format NONE = other -> other == Format.NONE;

    /// Whether this format is equal to (and thus compatible) to the given [Format].
    ///
    /// @param other the [Format] to compare against
    /// @return `true` if this format is compatible with the given [Format]
    boolean equals(Format other);

}
