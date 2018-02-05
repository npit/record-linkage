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

package BlockProcessing;

import BlockBuilding.AbstractBlockBuilding;
import DataModel.AbstractBlock;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */

public abstract class AbstractBlockProcessing implements IBlockProcessing {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractBlockBuilding.class.getName());
    
    protected void printOriginalStatistics(List<AbstractBlock> inputBlocks) {
        double comparisons = 0;
        comparisons = inputBlocks.stream().map((aBlock) -> aBlock.getNoOfComparisons()).reduce(comparisons, (accumulator, _item) -> accumulator + _item);
        
        LOGGER.log(Level.INFO, "Original blocks\t:\t{0}", inputBlocks.size());
        LOGGER.log(Level.INFO, "Original comparisons\t:\t{0}", comparisons);
    }
}
