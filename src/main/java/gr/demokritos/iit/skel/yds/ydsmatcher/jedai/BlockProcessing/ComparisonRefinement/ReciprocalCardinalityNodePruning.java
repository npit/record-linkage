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

import DataModel.Comparison;
import Utilities.Enumerations.WeightingScheme;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class ReciprocalCardinalityNodePruning extends CardinalityNodePruning {

    private static final Logger LOGGER = Logger.getLogger(ReciprocalCardinalityNodePruning.class.getName());
    
    public ReciprocalCardinalityNodePruning() {
        this(WeightingScheme.ARCS);
    }
    
    public ReciprocalCardinalityNodePruning(WeightingScheme scheme) {
        super(scheme);
        
        LOGGER.log(Level.INFO, "{0} initiated", getMethodName());
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": a Meta-blocking method that retains the comparisons "
               + "that correspond to edges in the blocking graph that are among the top-k weighted "
               + "ones for both adjacent entities/nodes.";
    }

    @Override
    public String getMethodName() {
        return "Reciprocal Cardinality Node Pruning";
    }

    @Override
    protected boolean isValidComparison(int entityId, Comparison comparison) {
        int neighborId = comparison.getEntityId1() == entityId ? comparison.getEntityId2() : comparison.getEntityId1();
        if (cleanCleanER && entityId < datasetLimit) {
            neighborId += datasetLimit;
        }

        if (nearestEntities[neighborId] == null) {
            return false;
        }

        if (nearestEntities[neighborId].contains(comparison)) {
            return entityId < neighborId;
        }

        return false;
    }
}
