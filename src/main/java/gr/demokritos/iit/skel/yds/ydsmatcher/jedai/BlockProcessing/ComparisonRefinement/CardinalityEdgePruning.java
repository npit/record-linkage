/*
* Copyright [2016-2017] [George Papadakis (gpapadis@yahoo.gr)]
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

package BlockProcessing.ComparisonRefinement;

import DataModel.AbstractBlock;
import DataModel.Comparison;
import DataModel.DecomposedBlock;
import Utilities.Comparators.ComparisonWeightComparator;
import Utilities.Enumerations.WeightingScheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */

public class CardinalityEdgePruning extends WeightedEdgePruning {

    private static final Logger LOGGER = Logger.getLogger(CardinalityEdgePruning.class.getName());
    
    protected double minimumWeight;
    protected Queue<Comparison> topKEdges;
    
    public CardinalityEdgePruning() {
        super(WeightingScheme.ARCS);
    }
    
    public CardinalityEdgePruning(WeightingScheme scheme) {
        super(scheme);
        nodeCentric = false;
        
        LOGGER.log(Level.INFO, "{0} initiated", getMethodName());
    }

    protected void addDecomposedBlock(Collection<Comparison> comparisons, List<AbstractBlock> newBlocks) {
        if (comparisons.isEmpty()) {
            return;
        }

        int[] entityIds1 = new int[comparisons.size()];
        int[] entityIds2 = new int[comparisons.size()];

        int index = 0;
        Iterator<Comparison> iterator = comparisons.iterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            entityIds1[index] = comparison.getEntityId1();
            entityIds2[index] = comparison.getEntityId2();
            index++;
        }

        newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": a Meta-blocking method that retains the comparisons "
               + "that correspond to the top-K weighted edges in the blocking graph.";
    }
    
    @Override
    public String getMethodName() {
        return "Cardinality Edge Pruning";
    }
    
    @Override
    protected List<AbstractBlock> pruneEdges() {
        minimumWeight = Double.MIN_VALUE;
        topKEdges = new PriorityQueue<>((int) (2 * threshold), new ComparisonWeightComparator());

        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                processArcsEntity(i);
                verifyValidEntities(i);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                processEntity(i);
                verifyValidEntities(i);
            }
        }

        List<AbstractBlock> newBlocks = new ArrayList<>();
        addDecomposedBlock(topKEdges, newBlocks);
        return newBlocks;
    }

    @Override
    protected void setThreshold() {
        threshold = blockAssingments / 2;
        
        LOGGER.log(Level.INFO, "Edge Pruning Cardinality Threshold\t:\t{0}", threshold);
    }

    protected void verifyValidEntities(int entityId) {
        validEntities.forEach((neighborId) -> {
            double weight = getWeight(entityId, neighborId);
            if (!(weight < minimumWeight)) {
                Comparison comparison = getComparison(entityId, neighborId);
                comparison.setUtilityMeasure(weight);
                topKEdges.add(comparison);
                if (threshold < topKEdges.size()) {
                    Comparison lastComparison = topKEdges.poll();
                    minimumWeight = lastComparison.getUtilityMeasure();
                }
            }
        });
    }
}
