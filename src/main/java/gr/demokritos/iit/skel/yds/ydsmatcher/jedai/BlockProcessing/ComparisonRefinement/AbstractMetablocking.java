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
import DataModel.BilateralBlock;
import DataModel.Comparison;
import DataModel.UnilateralBlock;
import Utilities.Enumerations.WeightingScheme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

/**
 *
 * @author G.A.P. II
 */
public abstract class AbstractMetablocking extends AbstractComparisonRefinementMethod {

    private static final Logger LOGGER = Logger.getLogger(AbstractMetablocking.class.getName());
    
    protected boolean nodeCentric;

    protected int[] flags;

    protected double threshold;
    protected double blockAssingments;
    protected double distinctComparisons;
    protected double[] comparisonsPerEntity;
    protected double[] counters;

    protected final List<Integer> neighbors;
    protected final List<Integer> retainedNeighbors;
    protected WeightingScheme weightingScheme;

    public AbstractMetablocking(WeightingScheme wScheme) {
        super();
        neighbors = new ArrayList<>();
        retainedNeighbors = new ArrayList<>();
        weightingScheme = wScheme;
    }

    protected abstract List<AbstractBlock> pruneEdges();

    protected abstract void setThreshold();

    @Override
    protected List<AbstractBlock> applyMainProcessing() {
        counters = new double[noOfEntities];
        flags = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            flags[i] = -1;
        }

        blockAssingments = 0;
        if (cleanCleanER) {
            for (BilateralBlock bBlock : bBlocks) {
                blockAssingments += bBlock.getTotalBlockAssignments();
            }
        } else {
            for (UnilateralBlock uBlock : uBlocks) {
                blockAssingments += uBlock.getTotalBlockAssignments();
            }
        }

        if (weightingScheme.equals(WeightingScheme.EJS)) {
            setStatistics();
        }

        setThreshold();
        return pruneEdges();
    }

    protected void freeMemory() {
        bBlocks = null;
        flags = null;
        counters = null;
        uBlocks = null;
    }

    protected Comparison getComparison(int entityId, int neighborId) {
        if (!cleanCleanER) {
            if (entityId < neighborId) {
                return new Comparison(cleanCleanER, entityId, neighborId);
            } else {
                return new Comparison(cleanCleanER, neighborId, entityId);
            }
        } else {
            if (entityId < datasetLimit) {
                return new Comparison(cleanCleanER, entityId, neighborId - datasetLimit);
            } else {
                return new Comparison(cleanCleanER, neighborId, entityId - datasetLimit);
            }
        }
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " involves a single parameter:\n"
                + "1)" + getParameterDescription(0) + ".\n";
    }

    protected int[] getNeighborEntities(int blockIndex, int entityId) {
        if (cleanCleanER) {
            if (entityId < datasetLimit) {
                return bBlocks[blockIndex].getIndex2Entities();
            } else {
                return bBlocks[blockIndex].getIndex1Entities();
            }
        }
        return uBlocks[blockIndex].getEntities();
    }

    @Override
    public JsonArray getParameterConfiguration() {
        JsonObject obj1 = new JsonObject();
        obj1.put("class", "Utilities.Enumerations.WeightingScheme");
        obj1.put("name", getParameterName(0));
        obj1.put("defaultValue", "Utilities.Enumerations.WeightingScheme.CBS");
        obj1.put("minValue", "-");
        obj1.put("maxValue", "-");
        obj1.put("stepValue", "-");
        obj1.put("description", getParameterDescription(0));

        JsonArray array = new JsonArray();
        array.add(obj1);
        return array;
    }

    @Override
    public String getParameterDescription(int parameterId) {
        switch (parameterId) {
            case 0:
                return "The " + getParameterName(0) + " determines the function that assigns weights to the edges of the Blocking Graph.";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "Weighting Scheme";
            default:
                return "invalid parameter id";
        }
    }

    protected double getWeight(int entityId, int neighborId) {
        switch (weightingScheme) {
            case ARCS:
                return counters[neighborId];
            case CBS:
                return counters[neighborId];
            case ECBS:
                return counters[neighborId] * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(entityId, 0)) * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(neighborId, 0));
            case JS:
                return counters[neighborId] / (entityIndex.getNoOfEntityBlocks(entityId, 0) + entityIndex.getNoOfEntityBlocks(neighborId, 0) - counters[neighborId]);
            case EJS:
                double probability = counters[neighborId] / (entityIndex.getNoOfEntityBlocks(entityId, 0) + entityIndex.getNoOfEntityBlocks(neighborId, 0) - counters[neighborId]);
                return probability * Math.log10(distinctComparisons / comparisonsPerEntity[entityId]) * Math.log10(distinctComparisons / comparisonsPerEntity[neighborId]);
        }
        return -1;
    }

    protected void setNormalizedNeighborEntities(int blockIndex, int entityId) {
        neighbors.clear();
        if (cleanCleanER) {
            if (entityId < datasetLimit) {
                for (int originalId : bBlocks[blockIndex].getIndex2Entities()) {
                    neighbors.add(originalId + datasetLimit);
                }
            } else {
                for (int originalId : bBlocks[blockIndex].getIndex1Entities()) {
                    neighbors.add(originalId);
                }
            }
        } else {
            if (!nodeCentric) {
                for (int neighborId : uBlocks[blockIndex].getEntities()) {
                    if (neighborId < entityId) {
                        neighbors.add(neighborId);
                    }
                }
            } else {
                for (int neighborId : uBlocks[blockIndex].getEntities()) {
                    if (neighborId != entityId) {
                        neighbors.add(neighborId);
                    }
                }
            }
        }
    }

    protected void setStatistics() {
        distinctComparisons = 0;
        comparisonsPerEntity = new double[noOfEntities];
        final Set<Integer> distinctNeighbors = new HashSet<>();
        for (int i = 0; i < noOfEntities; i++) {
            final int[] associatedBlocks = entityIndex.getEntityBlocks(i, 0);
            if (associatedBlocks.length != 0) {
                distinctNeighbors.clear();
                for (int blockIndex : associatedBlocks) {
                    for (int neighborId : getNeighborEntities(blockIndex, i)) {
                        distinctNeighbors.add(neighborId);
                    }
                }
                comparisonsPerEntity[i] = distinctNeighbors.size();
                if (!cleanCleanER) {
                    comparisonsPerEntity[i]--;
                }
                distinctComparisons += comparisonsPerEntity[i];
            }
        }
        distinctComparisons /= 2;
    }
}
