package io.github.kaktushose.proteus.type;

public interface Format {

    boolean equals(Format format);

    static None none() {
        return new None();
    }

    record None() implements Format {

        @Override
        public boolean equals(Format format) {
            return false;
        }
    }
}
