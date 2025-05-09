package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.graph.Graph;

public class ProteusBuilder {

    private int cacheSize;
    private boolean defaultMappers;
    private ConflictStrategy conflictStrategy;

    public ProteusBuilder() {
        cacheSize = 1000;
        defaultMappers = true;
        conflictStrategy = ConflictStrategy.FAIL;
    }

    public ProteusBuilder cacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public ProteusBuilder defaultMappers(boolean defaultMappers) {
        this.defaultMappers = defaultMappers;
        return this;
    }

    public ProteusBuilder conflictStrategy(ConflictStrategy conflictStrategy) {
        this.conflictStrategy = conflictStrategy;
        return this;
    }

    public Proteus build() {
        Graph graph = new Graph(cacheSize);
        Proteus p =  new Proteus(graph, conflictStrategy);

        if (defaultMappers) {
            LosslessDefaultMappers.registerMappers(p);
        }

        return p;
    }

    public enum ConflictStrategy {
        FAIL,
        IGNORE,
        OVERRIDE
    }

}
