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

package Utilities.DataStructures;

import DataModel.Comparison;
import DataModel.IdDuplicates;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author gap2
 */

public class BilateralDuplicatePropagation extends AbstractDuplicatePropagation {
 
    private Set<Integer> entities1;
    private Set<Integer> entities2;
    
    public BilateralDuplicatePropagation(Set<IdDuplicates> matches) {
        super(matches);
        entities1 = new HashSet<Integer>(2*existingDuplicates);
        entities2 = new HashSet<Integer>(2*existingDuplicates);
    }
    
    @Override
    public int getNoOfDuplicates() {
        return entities1.size();
    }
    
    @Override
    public boolean isSuperfluous(Comparison comparison) {
        Integer id1 = comparison.getEntityId1();
        Integer id2 = comparison.getEntityId2();
        if (entities1.contains(id1) || entities2.contains(id2)) {
            return true;
        }
        
        final IdDuplicates tempDuplicates = new IdDuplicates(id1, id2);
        if (duplicates.contains(tempDuplicates)) {
            entities1.add(id1);
            entities2.add(id2);
        }
        return false;
    }
    
    @Override
    public void resetDuplicates() {
        entities1 = new HashSet<Integer>(2*existingDuplicates);
        entities2 = new HashSet<Integer>(2*existingDuplicates);
    }
}