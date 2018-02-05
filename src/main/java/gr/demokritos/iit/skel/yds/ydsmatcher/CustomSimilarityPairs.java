package gr.demokritos.iit.skel.yds.ydsmatcher;

import DataModel.AbstractBlock;
import DataModel.SimilarityPairs;

import java.util.List;

public class CustomSimilarityPairs extends SimilarityPairs{
    private final double[] similarities;
    private final int[] entityIds1;
    private final int[] entityIds2;
    public CustomSimilarityPairs(boolean ccer, List<AbstractBlock> blocks, int[] ids1, int[] ids2, double[] sims) {
        super(ccer, blocks);
        this.entityIds1 = ids1;
        this.entityIds2 = ids2;
        this.similarities = sims;
    }
}
