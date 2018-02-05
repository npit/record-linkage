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

import BlockProcessing.AbstractBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.BilateralBlock;
import DataModel.UnilateralBlock;
import Utilities.Comparators.BlockCardinalityComparator;
import Utilities.Converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

/**
 *
 * @author gap2
 */

public class BlockFiltering extends AbstractBlockProcessing {

    private static final Logger LOGGER = Logger.getLogger(BlockFiltering.class.getName());
    
    protected final double ratio;

    protected int entitiesD1;
    protected int entitiesD2;
    protected int[] counterD1;
    protected int[] counterD2;
    protected int[] limitsD1;
    protected int[] limitsD2;
    
    public BlockFiltering() {
        this(0.8); 
        
        LOGGER.log(Level.INFO, "Using default configuration for {0}.", getMethodName());
    }
    
    public BlockFiltering(double r) {
        ratio = r;
        
        LOGGER.log(Level.INFO, getMethodConfiguration());
    }

    protected void countEntities(List<AbstractBlock> blocks) {
        entitiesD1 = Integer.MIN_VALUE;
        entitiesD2 = Integer.MIN_VALUE;
        if (blocks.get(0) instanceof BilateralBlock) {
            for (AbstractBlock block : blocks) {
                BilateralBlock bilBlock = (BilateralBlock) block;
                for (int id1 : bilBlock.getIndex1Entities()) {
                    if (entitiesD1 < id1 + 1) {
                        entitiesD1 = id1 + 1;
                    }
                }
                for (int id2 : bilBlock.getIndex2Entities()) {
                    if (entitiesD2 < id2 + 1) {
                        entitiesD2 = id2 + 1;
                    }
                }
            }
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            blocks.stream().map((block) -> (UnilateralBlock) block).forEachOrdered((uniBlock) -> {
                for (int id : uniBlock.getEntities()) {
                    if (entitiesD1 < id + 1) {
                        entitiesD1 = id + 1;
                    }
                }
            });
        }
    }

    protected void getBilateralLimits(List<AbstractBlock> blocks) {
        limitsD1 = new int[entitiesD1];
        limitsD2 = new int[entitiesD2];
        blocks.stream().map((block) -> (BilateralBlock) block).map((bilBlock) -> {
            for (int id1 : bilBlock.getIndex1Entities()) {
                limitsD1[id1]++;
            }
            return bilBlock;
        }).forEachOrdered((bilBlock) -> {
            for (int id2 : bilBlock.getIndex2Entities()) {
                limitsD2[id2]++;
            }
        });

        for (int i = 0; i < limitsD1.length; i++) {
            limitsD1[i] = (int) Math.round(ratio * limitsD1[i]);
        }
        for (int i = 0; i < limitsD2.length; i++) {
            limitsD2[i] = (int) Math.round(ratio * limitsD2[i]);
        }
    }
    
    @Override
    public String getMethodConfiguration() {
        return getParameterName(0) + "=" + ratio;
    }
    
    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it retains every entity in a subset of its smallest blocks.";
    }
    
    @Override
    public String getMethodName() {
        return "Block Filtering";
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " involves a single parameter:\n"
               + "1)" + getParameterDescription(0) + ".\n";
    }

    protected void getLimits(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof BilateralBlock) {
            getBilateralLimits(blocks);
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            getUnilateralLimits(blocks);
        }
    }

    protected void getUnilateralLimits(List<AbstractBlock> blocks) {
        limitsD1 = new int[entitiesD1];
        limitsD2 = null;
        blocks.stream().map((block) -> (UnilateralBlock) block).forEachOrdered((uniBlock) -> {
            for (int id : uniBlock.getEntities()) {
                limitsD1[id]++;
            }
        });

        for (int i = 0; i < limitsD1.length; i++) {
            limitsD1[i] = (int) Math.round(ratio * limitsD1[i]);
        }
    }
    
    @Override
    public JsonArray getParameterConfiguration() {
        JsonObject obj = new JsonObject();
        obj.put("class", "java.lang.Double");
        obj.put("name", getParameterName(0));
        obj.put("defaultValue", "0.8");
        obj.put("minValue", "0.05");
        obj.put("maxValue", "1.0");
        obj.put("stepValue", "0.05");
        obj.put("description", getParameterDescription(0));
        
        JsonArray array = new JsonArray();
        array.add(obj);
        
        return array;
    }

    @Override
    public String getParameterDescription(int parameterId) {
        switch(parameterId) {
            case 0:
                return "The " + getParameterName(0) + " specifies the portion of the retained smaller blocks per entity.";
            default:
                return "invalid parameter id";
        }
    }
    
    @Override
    public String getParameterName(int parameterId) {
        switch(parameterId) {
            case 0:
                return "Filtering Ratio";
            default:
                return "invalid parameter id";
        }
    }
    
    protected void initializeCounters() {
        counterD1 = new int[entitiesD1];
        counterD2 = null;
        if (0 < entitiesD2) {
            counterD2 = new int[entitiesD2];
        }
    }

    @Override
    public List<AbstractBlock> refineBlocks(List<AbstractBlock> blocks) {
        printOriginalStatistics(blocks);
        countEntities(blocks);
        sortBlocks(blocks);
        getLimits(blocks);
        initializeCounters();
        return restructureBlocks(blocks);
    }
    
    protected List<AbstractBlock> restructureBilateraBlocks(List<AbstractBlock> blocks) {
        final List<AbstractBlock> newBlocks = new ArrayList<>();
        blocks.stream().map((block) -> (BilateralBlock) block).forEachOrdered((oldBlock) -> {
            final List<Integer> retainedEntitiesD1 = new ArrayList<>();
            for (int entityId : oldBlock.getIndex1Entities()) {
                if (counterD1[entityId] < limitsD1[entityId]) {
                    retainedEntitiesD1.add(entityId);
                }
            }
            final List<Integer> retainedEntitiesD2 = new ArrayList<>();
            for (int entityId : oldBlock.getIndex2Entities()) {
                if (counterD2[entityId] < limitsD2[entityId]) {
                    retainedEntitiesD2.add(entityId);
                }
            }
            if (!retainedEntitiesD1.isEmpty() && !retainedEntitiesD2.isEmpty()) {
                int[] blockEntitiesD1 = Converter.convertCollectionToArray(retainedEntitiesD1);
                for (int entityId : blockEntitiesD1) {
                    counterD1[entityId]++;
                }
                int[] blockEntitiesD2 = Converter.convertCollectionToArray(retainedEntitiesD2);
                for (int entityId : blockEntitiesD2) {
                    counterD2[entityId]++;
                }
                newBlocks.add(new BilateralBlock(blockEntitiesD1, blockEntitiesD2));
            }
        });
        
        return newBlocks;
    }

    protected List<AbstractBlock> restructureBlocks(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof BilateralBlock) {
            return restructureBilateraBlocks(blocks);
        } 
            
        return restructureUnilateraBlocks(blocks);
    }

    protected List<AbstractBlock> restructureUnilateraBlocks(List<AbstractBlock> blocks) {
        final List<AbstractBlock> newBlocks = new ArrayList<>();
        blocks.stream().map((block) -> (UnilateralBlock) block).map((oldBlock) -> {
            final List<Integer> retainedEntities = new ArrayList<>();
            for (int entityId : oldBlock.getEntities()) {
                if (counterD1[entityId] < limitsD1[entityId]) {
                    retainedEntities.add(entityId);
                }
            }
            return retainedEntities;
        }).filter((retainedEntities) -> (1 < retainedEntities.size())).map((retainedEntities) -> Converter.convertCollectionToArray(retainedEntities)).map((blockEntities) -> {
            for (int entityId : blockEntities) {
                counterD1[entityId]++;
            }
            return blockEntities;
        }).forEachOrdered((blockEntities) -> {
            newBlocks.add(new UnilateralBlock(blockEntities));
        });
        return newBlocks;
    }

    protected void sortBlocks(List<AbstractBlock> blocks) {
        Collections.sort(blocks, new BlockCardinalityComparator());
    }
}
