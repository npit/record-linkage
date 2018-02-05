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
public class WeightedEdgePruning extends AbstractMetablocking {

    private static final Logger LOGGER = Logger.getLogger(WeightedEdgePruning.class.getName());
    
    protected double noOfEdges;

    public WeightedEdgePruning() {
        this(WeightingScheme.CBS);
    }
    
    public WeightedEdgePruning(WeightingScheme scheme) {
        super(scheme);
        nodeCentric = false;
        
        LOGGER.log(Level.INFO, "{0} initiated", getMethodName());
    }

    @Override
    public String getMethodConfiguration() {
        return getParameterName(0) + "=" + weightingScheme;
    }
    
    @Override
    public String getMethodInfo() {
        return getMethodName() + ": a Meta-blocking method that retains all comparisons "
                + "that have a weight higher than the average edge weight in the blocking graph.";
    }

    @Override
    public String getMethodName() {
        return "Weighted Edge Pruning";
    }
    
    protected void processArcsEntity(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            double blockComparisons = cleanCleanER ? bBlocks[blockIndex].getNoOfComparisons() : uBlocks[blockIndex].getNoOfComparisons();
            setNormalizedNeighborEntities(blockIndex, entityId);
            neighbors.stream().map((neighborId) -> {
                if (flags[neighborId] != entityId) {
                    counters[neighborId] = 0;
                    flags[neighborId] = entityId;
                }
                return neighborId;
            }).map((neighborId) -> {
                counters[neighborId] += 1 / blockComparisons;
                return neighborId;
            }).forEachOrdered((neighborId) -> {
                validEntities.add(neighborId);
            });
        }
    }

    protected void processEntity(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            setNormalizedNeighborEntities(blockIndex, entityId);
            neighbors.stream().map((neighborId) -> {
                if (flags[neighborId] != entityId) {
                    counters[neighborId] = 0;
                    flags[neighborId] = entityId;
                }
                return neighborId;
            }).map((neighborId) -> {
                counters[neighborId]++;
                return neighborId;
            }).forEachOrdered((neighborId) -> {
                validEntities.add(neighborId);
            });
        }
    }

    @Override
    protected List<AbstractBlock> pruneEdges() {
        List<AbstractBlock> newBlocks = new ArrayList<>();
        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                processArcsEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                processEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        }
        return newBlocks;
    }

    @Override
    protected void setThreshold() {
        noOfEdges = 0;
        threshold = 0;

        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                processArcsEntity(i);
                updateThreshold(i);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                processEntity(i);
                updateThreshold(i);
            }
        }

        threshold /= noOfEdges;
        
        LOGGER.log(Level.INFO, "Edge Pruning Weight Threshold\t:\t{0}", threshold);
    }

    protected void updateThreshold(int entityId) {
        noOfEdges += validEntities.size();
        validEntities.forEach((neighborId) -> {
            threshold += getWeight(entityId, neighborId);
        });
    }

    protected void verifyValidEntities(int entityId, List<AbstractBlock> newBlocks) {
        retainedNeighbors.clear();
        if (!cleanCleanER) {
            validEntities.forEach((neighborId) -> {
                double weight = getWeight(entityId, neighborId);
                if (threshold <= weight) {
                    retainedNeighbors.add(neighborId);
                }
            });
            addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
        } else {
            if (entityId < datasetLimit) {
                validEntities.forEach((neighborId) -> {
                    double weight = getWeight(entityId, neighborId);
                    if (threshold <= weight) {
                        retainedNeighbors.add(neighborId-datasetLimit);
                    }
                });
                addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
            } else {
                validEntities.forEach((neighborId) -> {
                    double weight = getWeight(entityId, neighborId);
                    if (threshold <= weight) {
                        retainedNeighbors.add(neighborId);
                    }
                });
                addReversedDecomposedBlock(entityId-datasetLimit, retainedNeighbors, newBlocks);
            }
        }
    }
}
