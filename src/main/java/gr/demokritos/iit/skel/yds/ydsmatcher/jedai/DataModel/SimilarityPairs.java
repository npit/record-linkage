/*
* Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package DataModel;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class SimilarityPairs {

    private final static int MAX_COMPARISONS = Integer.MAX_VALUE - 2;
    private static final Logger LOGGER = Logger.getLogger(SimilarityPairs.class.getName());

    private final boolean isCleanCleanER;
    private int currentIndex;

    public void setSimilarities(double[] similarities) {
        this.similarities = similarities;
    }

    private double[] similarities;

    public void setEntityIds1(int[] entityIds1) {
        this.entityIds1 = entityIds1;
    }

    public void setEntityIds2(int[] entityIds2) {
        this.entityIds2 = entityIds2;
    }

    private int[] entityIds1;
    private int[] entityIds2;

    public SimilarityPairs(boolean ccer, List<AbstractBlock> blocks) {
        isCleanCleanER = ccer;
        double totalComparisons = countComparisons(blocks);
        entityIds1 = new int[(int) totalComparisons];
        entityIds2 = new int[(int) totalComparisons];
        similarities = new double[(int) totalComparisons];
    }

    public void addComparison(Comparison comparison) {
        entityIds1[currentIndex] = comparison.getEntityId1();
        entityIds2[currentIndex] = comparison.getEntityId2();
        similarities[currentIndex++] = comparison.getUtilityMeasure();
    }

    private double countComparisons(List<AbstractBlock> blocks) {
        double comparisons = 0;
        for (AbstractBlock block : blocks) {
            comparisons += block.getNoOfComparisons();
        }

        if (MAX_COMPARISONS < comparisons) {
            LOGGER.log(Level.SEVERE, "Very high number of comparisons to be executed! "
                    + "Maximum allowed number is : " + MAX_COMPARISONS);
            System.exit(-1);
        }
        return comparisons;
    }

    public int[] getEntityIds1() {
        return entityIds1;
    }

    public int[] getEntityIds2() {
        return entityIds2;
    }

    public int getNoOfComparisons() {
        return currentIndex;
    }

    public PairIterator getPairIterator() {
        return new PairIterator(this);
    }

    public double[] getSimilarities() {
        return similarities;
    }

    public boolean isCleanCleanER() {
        return isCleanCleanER;
    }
}
