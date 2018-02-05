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

import BlockBuilding.AbstractBlockBuilding;
import BlockProcessing.AbstractBlockProcessing;
import DataModel.AbstractBlock;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */

public abstract class AbstractBlockPurging extends AbstractBlockProcessing {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractBlockBuilding.class.getName());

    public AbstractBlockPurging() {
    }

    @Override
    public List<AbstractBlock> refineBlocks(List<AbstractBlock> blocks) {
        printOriginalStatistics(blocks);
        setThreshold(blocks);

        int noOfPurgedBlocks = 0;
        double totalComparisons = 0;
        final Iterator<AbstractBlock> blocksIterator = blocks.iterator();
        while (blocksIterator.hasNext()) {
            AbstractBlock aBlock = blocksIterator.next();
            if (satisfiesThreshold(aBlock)) {
                noOfPurgedBlocks++;
                blocksIterator.remove();
            } else {
                totalComparisons += aBlock.getNoOfComparisons();
            }
        }
        
        LOGGER.log(Level.INFO, "Purged blocks\t:\t{0}", noOfPurgedBlocks);
        LOGGER.log(Level.INFO, "Retained blocks\t:\t{0}", blocks.size());
        LOGGER.log(Level.INFO, "Retained comparisons\t:\t{0}", totalComparisons);

        return blocks;
    }
    
    protected abstract boolean satisfiesThreshold(AbstractBlock block);
    protected abstract void setThreshold(List<AbstractBlock> blocks);
}
