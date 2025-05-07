package io.github.kaktushose.proteus;

import io.github.kaktushose.proteus.graph.Graph;

public class ProteusBuilder {

    private static final Graph sharedGraph = new Graph(1000);
    private int cacheSize;
    private boolean defaultMappers;
    private boolean useSharedGraph;
    private ConflictStrategy conflictStrategy;

    public ProteusBuilder() {
        cacheSize = 1000;
        defaultMappers = true;
        useSharedGraph = true;
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

    public ProteusBuilder sharedGraph(boolean sharedGraph) {
        this.useSharedGraph = sharedGraph;
        return this;
    }

    public ProteusBuilder setConflictStrategy(ConflictStrategy conflictStrategy) {
        this.conflictStrategy = conflictStrategy;
        return this;
    }

    public Proteus build() {
        Graph graph = useSharedGraph ? sharedGraph : new Graph(cacheSize);
        if (defaultMappers) {
            LosslessDefaultMappers.registerMappers(graph);
        }
        return new Proteus(graph, conflictStrategy);
    }

    public enum ConflictStrategy {
        FAIL,
        IGNORE,
        OVERRIDE
    }

}
