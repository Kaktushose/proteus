package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.type.Format;

record TestFormat(String format) implements Format {

    @Override
    public boolean equals(Format other) {
        if (other instanceof TestFormat(String otherFormat)) {
            return format.equals(otherFormat);
        }
        return false;
    }

}
