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
import java.util.Arrays;

/**
 *
 * @author G.A.P. II
 */

public class BilateralBlock extends AbstractBlock implements Serializable {

    private static final long serialVersionUID = 75264711552351524L;
    
    private final int[] index1Entities;
    private final int[] index2Entities;

    public BilateralBlock(int[] entities1, int[] entities2) {
        super();
        index1Entities = entities1;
        index2Entities = entities2;
        comparisons = ((double) index1Entities.length) * ((double) index2Entities.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BilateralBlock other = (BilateralBlock) obj;
        if (!Arrays.equals(this.index1Entities, other.index1Entities)) {
            return false;
        }
        if (!Arrays.equals(this.index2Entities, other.index2Entities)) {
            return false;
        }
        return true;
    }

    public int[] getIndex1Entities() {
        return index1Entities;
    }

    public int[] getIndex2Entities() {
        return index2Entities;
    }

    @Override
    public double getTotalBlockAssignments() {
        return index1Entities.length+index2Entities.length;
    }
        
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Arrays.hashCode(this.index1Entities);
        hash = 41 * hash + Arrays.hashCode(this.index2Entities);
        return hash;
    }
    
    @Override
    public void setUtilityMeasure() {
        utilityMeasure = 1.0/Math.max(index1Entities.length, index2Entities.length);
    }
}