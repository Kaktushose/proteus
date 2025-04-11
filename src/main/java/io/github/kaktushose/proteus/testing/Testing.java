package io.github.kaktushose.proteus.testing;

import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.adapter.impl.IntegerAdapter;
import io.github.kaktushose.proteus.adapter.impl.LongAdapter;
import io.github.kaktushose.proteus.adapter.impl.ShortAdapter;

public class Testing {

    public static void main(String[] args) {
        Proteus proteus = new Proteus();

        proteus.register(String.class, Long.class, new LongAdapter());
        proteus.register(Long.class, Integer.class, new IntegerAdapter());
        proteus.register(Integer.class, Short.class, new ShortAdapter());

        System.out.println(proteus.convert("5", Short.class));
    }
}
