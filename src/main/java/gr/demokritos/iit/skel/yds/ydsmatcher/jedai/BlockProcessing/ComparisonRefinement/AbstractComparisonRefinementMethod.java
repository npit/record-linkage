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

import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.BilateralBlock;
import DataModel.DecomposedBlock;
import DataModel.UnilateralBlock;
import Utilities.DataStructures.EntityIndex;
import Utilities.Converter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */

public abstract class AbstractComparisonRefinementMethod implements IBlockProcessing {

    private static final Logger LOGGER = Logger.getLogger(AbstractComparisonRefinementMethod.class.getName());
    
    protected boolean cleanCleanER;

    protected int datasetLimit;
    protected int noOfBlocks;
    protected int noOfEntities;

    protected EntityIndex entityIndex;
    protected BilateralBlock[] bBlocks;
    protected final Set<Integer> validEntities;
    protected UnilateralBlock[] uBlocks;

    public AbstractComparisonRefinementMethod() {
        validEntities = new HashSet<>();
    }

    protected void addDecomposedBlock(int entityId, Collection<Integer> neighbors, List<AbstractBlock> newBlocks) {
        if (neighbors.isEmpty()) {
            return;
        }

        int[] entityIds1 = replicateId(entityId, neighbors.size());
        int[] entityIds2 = Converter.convertCollectionToArray(neighbors);
        newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
    }
    
    protected void addReversedDecomposedBlock(int entityId, Collection<Integer> neighbors, List<AbstractBlock> newBlocks) {
        if (neighbors.isEmpty()) {
            return;
        }

        int[] entityIds1 = Converter.convertCollectionToArray(neighbors);
        int[] entityIds2 = replicateId(entityId, neighbors.size());
        newBlocks.add(new DecomposedBlock(cleanCleanER, entityIds1, entityIds2));
    }
    
    protected abstract List<AbstractBlock> applyMainProcessing();

    @Override
    public List<AbstractBlock> refineBlocks(List<AbstractBlock> blocks) {
        entityIndex = new EntityIndex(blocks);
        
        cleanCleanER = entityIndex.isCleanCleanER();
        datasetLimit = entityIndex.getDatasetLimit();
        noOfBlocks = blocks.size();
        noOfEntities = entityIndex.getNoOfEntities();
        bBlocks = entityIndex.getBilateralBlocks();
        uBlocks = entityIndex.getUnilateralBlocks();

        return applyMainProcessing();
    }
    
    protected int[] replicateId(int entityId, int times) {
        int counter = 0;
        int[] array = new int[times];
        for (int i = 0; i < times; i++) {
            array[counter++] = entityId;
        }
        return array;
    }
}
