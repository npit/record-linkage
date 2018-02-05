/*
* Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
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

package DataModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */

public abstract class AbstractBlock implements Serializable {
    
    private static final long serialVersionUID = 7526443743449L;
    
    protected double comparisons;
    protected int blockIndex;
    protected double utilityMeasure;
            
    public AbstractBlock() {
        blockIndex = -1;
        utilityMeasure = -1;
    }
    
    public int getBlockIndex() {
        return blockIndex;
    }
    
    public ComparisonIterator getComparisonIterator() {
        return new ComparisonIterator(this);
    }

    public double getNoOfComparisons() {
        return comparisons;
    }
    
    public double getUtilityMeasure() {
        return utilityMeasure;
    }
    
    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }
    
    public List<Comparison> getComparisons() {
        final List<Comparison> comparisonsList = new ArrayList<>();

        ComparisonIterator iterator = getComparisonIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            comparisonsList.add(comparison);
        }
        
        return comparisonsList;
    }
    
    public abstract double getTotalBlockAssignments();
    public abstract void setUtilityMeasure();
}