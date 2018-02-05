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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.atlas.json.JsonArray;

/**
 *
 * @author gap2
 */
public class ComparisonPropagation extends AbstractComparisonRefinementMethod {

    private static final Logger LOGGER = Logger.getLogger(ComparisonPropagation.class.getName());
    
    public ComparisonPropagation() {
        super();
        
        LOGGER.log(Level.INFO, "{0} initiated", getMethodName());
    }

    @Override
    protected List<AbstractBlock> applyMainProcessing() {
        List<AbstractBlock> newBlocks = new ArrayList<>();
        if (cleanCleanER) {
            processBilateralBlocks(newBlocks);
        } else {
            processUnilateralBlocks(newBlocks);
        }
        return newBlocks;
    }

    @Override
    public String getMethodConfiguration() {
        return PARAMETER_FREE;
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it eliminates all redundant comparisons from a set of overlapping blocks.";
    }

    @Override
    public String getMethodName() {
        return "Comparison Propagation";
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " is a " + PARAMETER_FREE + ".";
    }

    @Override
    public JsonArray getParameterConfiguration() {
        return new JsonArray();
    }

    @Override
    public String getParameterDescription(int parameterId) {
        return PARAMETER_FREE;
    }

    @Override
    public String getParameterName(int parameterId) {
        return PARAMETER_FREE;
    }

    private void processBilateralBlocks(List<AbstractBlock> newBlocks) {
        for (int i = 0; i < datasetLimit; i++) {
            final int[] associatedBlocks = entityIndex.getEntityBlocks(i, 0);
            if (associatedBlocks.length != 0) {
                validEntities.clear();
                for (int blockIndex : associatedBlocks) {
                    for (int neighborId : bBlocks[blockIndex].getIndex2Entities()) {
                        validEntities.add(neighborId);
                    }
                }
                addDecomposedBlock(i, validEntities, newBlocks);
            }
        }
    }

    private void processUnilateralBlocks(List<AbstractBlock> newBlocks) {
        for (int i = 0; i < noOfEntities; i++) {
            final int[] associatedBlocks = entityIndex.getEntityBlocks(i, 0);
            if (associatedBlocks.length != 0) {
                validEntities.clear();
                for (int blockIndex : associatedBlocks) {
                    for (int neighborId : uBlocks[blockIndex].getEntities()) {
                        if (neighborId < i) {
                            validEntities.add(neighborId);
                        }
                    }
                }
                addDecomposedBlock(i, validEntities, newBlocks);
            }
        }
    }
}
