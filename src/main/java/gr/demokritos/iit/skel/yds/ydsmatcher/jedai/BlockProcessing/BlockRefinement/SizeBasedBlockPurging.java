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
import DataModel.BilateralBlock;
import DataModel.UnilateralBlock;

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

public class SizeBasedBlockPurging extends AbstractBlockPurging {
    
    private static final Logger LOGGER = Logger.getLogger(SizeBasedBlockPurging.class.getName());
    
    private boolean isCleanCleanER;
    private double purgingFactor;
    private double maxEntities;
    
    public SizeBasedBlockPurging() {
        this(0.005);
        
        LOGGER.log(Level.INFO, "Using default configuration for {0}.", getMethodName());
    }

    public SizeBasedBlockPurging(double pf) {
        purgingFactor = pf;
        
        LOGGER.log(Level.INFO, getMethodConfiguration());
    }
    
    private int getMaxBlockSize(List<AbstractBlock> blocks) {
        final Set<Integer> entities = new HashSet<>();
        blocks.stream().map((aBlock) -> (UnilateralBlock) aBlock).forEachOrdered((uBlock) -> {
            for (int id1 : uBlock.getEntities()) {
                entities.add(id1);
            }
        });
        
        return (int) Math.round(entities.size()*purgingFactor);
    }
    
    private int getMaxInnerBlockSize(List<AbstractBlock> blocks) {
        final Set<Integer> d1Entities = new HashSet<>();
        final Set<Integer> d2Entities = new HashSet<>();
        blocks.stream().map((block) -> (BilateralBlock) block).map((bilBlock) -> {
            for (int id1 : bilBlock.getIndex1Entities()) {
                d1Entities.add(id1);
            }
            return bilBlock;            
        }).forEachOrdered((bilBlock) -> {
            for (int id2 : bilBlock.getIndex2Entities()) {
                d2Entities.add(id2);
            }
        });
        
        return (int) Math.round(Math.min(d1Entities.size(), d2Entities.size())*purgingFactor);
    }
    
    @Override
    public String getMethodConfiguration() {
        return getParameterName(0) + "=" + purgingFactor;
    }
    
    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it discards the blocks exceeding a certain number of entities.";
    }
    
    @Override
    public String getMethodName() {
        return "Size-based Block Purging";
    }

    @Override
    public String getMethodParameters() {
        return  getMethodName() + " involves a single parameter:\n"
                + "1)" + getParameterDescription(0) + ".\n";
    }

    @Override
    public JsonArray getParameterConfiguration() {
        JsonObject obj = new JsonObject();
        obj.put("class", "java.lang.Double");
        obj.put("name", getParameterName(0));
        obj.put("defaultValue", "0.005");
        obj.put("minValue", "0.001");
        obj.put("maxValue", "0.100");
        obj.put("stepValue", "0.001");
        obj.put("description", getParameterDescription(0));
        
        JsonArray array = new JsonArray();
        array.add(obj);
        
        return array;
    }
    
    @Override
    public String getParameterDescription(int parameterId) {
        switch(parameterId) {
            case 0:
                return "The " + getParameterName(0) + " determines indirectly the maximum number of entities per block.";
            default:
                return "invalid parameter id";
        }
    }
    
    @Override
    public String getParameterName(int parameterId) {
        switch(parameterId) {
            case 0:
                return "Purging Factor";
            default:
                return "invalid parameter id";
        }
    }
    
    @Override
    protected boolean satisfiesThreshold(AbstractBlock block) {
        if (isCleanCleanER) {
            BilateralBlock bBlock = (BilateralBlock) block;
            return Math.min(bBlock.getIndex1Entities().length, bBlock.getIndex2Entities().length) <= maxEntities;
        } 
        return block.getTotalBlockAssignments() <= maxEntities;
    }

    @Override
    protected void setThreshold(List<AbstractBlock> blocks) {
        if (blocks.get(0) instanceof UnilateralBlock) {
            isCleanCleanER = false;
            maxEntities = getMaxBlockSize(blocks);
            LOGGER.log(Level.INFO, "Maximum entities per block\t:\t{0}", maxEntities);
        } else {
            isCleanCleanER = true;
            maxEntities = getMaxInnerBlockSize(blocks);
            LOGGER.log(Level.INFO, "Maximum inner block size per block\t:\t{0}", maxEntities);
        }
    }
}