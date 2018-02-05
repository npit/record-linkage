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
package BlockProcessing.BlockRefinement;

import DataModel.AbstractBlock;
import Utilities.Comparators.BlockCardinalityComparator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

/**
 *
 * @author gap2
 */
public class ComparisonsBasedBlockPurging extends AbstractBlockPurging {

    private static final Logger LOGGER = Logger.getLogger(ComparisonsBasedBlockPurging.class.getName());

    private double smoothingFactor;
    private double maxComparisonsPerBlock;

    public ComparisonsBasedBlockPurging() {
        this(1.025);

        LOGGER.log(Level.INFO, "Using default configuration for {0}.", getMethodName());
    }

    public ComparisonsBasedBlockPurging(double sf) {
        smoothingFactor = sf;

        LOGGER.log(Level.INFO, getMethodConfiguration());
    }

    @Override
    public String getMethodConfiguration() {
        return getParameterName(0) + "=" + smoothingFactor;
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it discards the blocks exceeding a certain number of comparisons.";
    }

    @Override
    public String getMethodName() {
        return "Comparison-based Block Purging";
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " involves a single parameter:\n"
                + "1)" + getParameterDescription(0) + ".\n";
    }

    @Override
    public JsonArray getParameterConfiguration() {
        JsonObject obj = new JsonObject();
        obj.put("class", "java.lang.Double");
        obj.put("name", getParameterName(0));
        obj.put("defaultValue", "1.025");
        obj.put("minValue", "1.0");
        obj.put("maxValue", "2.0");
        obj.put("stepValue", "0.01");
        obj.put("description", getParameterDescription(0));

        JsonArray array = new JsonArray();
        array.add(obj);

        return array;
    }

    @Override
    public String getParameterDescription(int parameterId) {
        switch (parameterId) {
            case 0:
                return "The " + getParameterName(0) + " determines the termination criterion for automatically estimating the "
                        + "maximum number of comparisons per block.";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "Smoothing Factor";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    protected boolean satisfiesThreshold(AbstractBlock block) {
        return block.getNoOfComparisons() <= maxComparisonsPerBlock;
    }

    @Override
    protected void setThreshold(List<AbstractBlock> blocks) {
        Collections.sort(blocks, new BlockCardinalityComparator());
        final Set<Double> distinctComparisonsLevel = new HashSet<>();
        blocks.forEach((block) -> {
            distinctComparisonsLevel.add(block.getNoOfComparisons());
        });

        int index = -1;
        double[] blockAssignments = new double[distinctComparisonsLevel.size()];
        double[] comparisonsLevel = new double[distinctComparisonsLevel.size()];
        double[] totalComparisonsPerLevel = new double[distinctComparisonsLevel.size()];
        for (AbstractBlock block : blocks) {
            if (index == -1) {
                index++;
                comparisonsLevel[index] = block.getNoOfComparisons();
                blockAssignments[index] = 0;
                totalComparisonsPerLevel[index] = 0;
            } else if (block.getNoOfComparisons() != comparisonsLevel[index]) {
                index++;
                comparisonsLevel[index] = block.getNoOfComparisons();
                blockAssignments[index] = blockAssignments[index - 1];
                totalComparisonsPerLevel[index] = totalComparisonsPerLevel[index - 1];
            }

            blockAssignments[index] += block.getTotalBlockAssignments();
            totalComparisonsPerLevel[index] += block.getNoOfComparisons();
        }

        double currentBC = 0;
        double currentCC = 0;
        double currentSize = 0;
        double previousBC = 0;
        double previousCC = 0;
        double previousSize = 0;
        int arraySize = blockAssignments.length;
        for (int i = arraySize - 1; 0 <= i; i--) {
            previousSize = currentSize;
            previousBC = currentBC;
            previousCC = currentCC;

            currentSize = comparisonsLevel[i];
            currentBC = blockAssignments[i];
            currentCC = totalComparisonsPerLevel[i];

            if (currentBC * previousCC < smoothingFactor * currentCC * previousBC) {
                break;
            }
        }

        maxComparisonsPerBlock = previousSize;
        LOGGER.log(Level.INFO, "Maximum comparisons per block\t:\t{0}", maxComparisonsPerBlock);
    }
}
