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

public class UnilateralDuplicatePropagation extends AbstractDuplicatePropagation {
 
    private Set<IdDuplicates> detectedDuplicates;
    
    public UnilateralDuplicatePropagation (Set<IdDuplicates> matches) {
        super(matches);
        detectedDuplicates = new HashSet<IdDuplicates>(2*matches.size());
    }
    
    @Override
    public int getNoOfDuplicates() {
        return detectedDuplicates.size();
    }
    
    @Override
    public boolean isSuperfluous(Comparison comparison) {
        final IdDuplicates duplicatePair1 = new IdDuplicates(comparison.getEntityId1(), comparison.getEntityId2());
        final IdDuplicates duplicatePair2 = new IdDuplicates(comparison.getEntityId2(), comparison.getEntityId1());
        if (detectedDuplicates.contains(duplicatePair1) || 
                detectedDuplicates.contains(duplicatePair2)) {
            return true;
        }
                
        if (duplicates.contains(duplicatePair1) || 
                duplicates.contains(duplicatePair2)) {
            if (comparison.getEntityId1() < comparison.getEntityId2()) {
                detectedDuplicates.add(new IdDuplicates(comparison.getEntityId1(), comparison.getEntityId2()));
            } else {
                detectedDuplicates.add(new IdDuplicates(comparison.getEntityId2(), comparison.getEntityId1()));
            }
        }
                    
        return false;
    }

    @Override
    public void resetDuplicates() {
        detectedDuplicates = new HashSet<IdDuplicates>();
    }
}