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
import Utilities.Enumerations.WeightingScheme;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */

public class WeightedNodePruning extends WeightedEdgePruning {

    private static final Logger LOGGER = Logger.getLogger(WeightedNodePruning.class.getName());
    
    protected int firstId;
    protected int lastId;
    protected double[] averageWeight;
    
    public WeightedNodePruning() {
        this(WeightingScheme.ARCS);
    }
    
    public WeightedNodePruning(WeightingScheme scheme) {
        super(scheme);
        nodeCentric = true;
        
        LOGGER.log(Level.INFO, "{0} initiated", getMethodName());
    }
    
    @Override
    public String getMethodInfo() {
        return getMethodName() + ": a Meta-blocking method that retains for every entity, the comparisons "
               + "that correspond to edges in the blocking graph that are exceed the average edge weight "
               + "in the respective node neighborhood.";
    }

    @Override
    public String getMethodName() {
        return "Weighted Node Pruning";
    }
        
    protected boolean isValidComparison(int entityId, int neighborId) {
        double weight = getWeight(entityId, neighborId);
        boolean inNeighborhood1 = averageWeight[entityId] <= weight;
        boolean inNeighborhood2 = averageWeight[neighborId] <= weight;

        if (inNeighborhood1 || inNeighborhood2) {
            return entityId < neighborId;
        }
        
        return false;
    }

    @Override
    protected List<AbstractBlock> pruneEdges() {
        List<AbstractBlock> newBlocks = new ArrayList<>();
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < noOfEntities; i++) {
                processArcsEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        } else {
            for (int i = 0; i < noOfEntities; i++) {
                processEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        }
        return newBlocks;
    }

    protected void setLimits() {
        firstId = 0;
        lastId = noOfEntities;
    }

    @Override
    protected void setThreshold() {
        averageWeight = new double[noOfEntities];
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < noOfEntities; i++) {
                processArcsEntity(i);
                setThreshold(i);
                averageWeight[i] = threshold;
            }
        } else {
            for (int i = 0; i < noOfEntities; i++) {
                processEntity(i);
                setThreshold(i);
                averageWeight[i] = threshold;
            }
        }
    }

    protected void setThreshold(int entityId) {
        threshold = 0;
        validEntities.forEach((neighborId) -> {
            threshold += getWeight(entityId, neighborId);
        });
        threshold /= validEntities.size();
    }

    @Override
    protected void verifyValidEntities(int entityId, List<AbstractBlock> newBlocks) {
        retainedNeighbors.clear();
        if (!cleanCleanER) {
            validEntities.stream().filter((neighborId) -> (isValidComparison(entityId, neighborId))).forEachOrdered((neighborId) -> {
                retainedNeighbors.add(neighborId);
            });
            addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
        } else {
            if (entityId < datasetLimit) {
                validEntities.stream().filter((neighborId) -> (isValidComparison(entityId, neighborId))).forEachOrdered((neighborId) -> {
                    retainedNeighbors.add(neighborId - datasetLimit);
                });
                addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
            } else {
                validEntities.stream().filter((neighborId) -> (isValidComparison(entityId, neighborId))).forEachOrdered((neighborId) -> {
                    retainedNeighbors.add(neighborId);
                });
                addReversedDecomposedBlock(entityId - datasetLimit, retainedNeighbors, newBlocks);
            }
        }
    }
}
