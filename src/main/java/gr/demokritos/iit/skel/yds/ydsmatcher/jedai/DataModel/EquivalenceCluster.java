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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */

public class EquivalenceCluster {
    
    private final List<Integer> entityIdsD1;
    private final List<Integer> entityIdsD2;
    
    public EquivalenceCluster() {
        entityIdsD1 = new ArrayList<>();
        entityIdsD2 = new ArrayList<>();
    }
    
    public void addEntityIdD1(int id) {
        entityIdsD1.add(id);
    }
    
    public void addEntityIdD2(int id) {
        entityIdsD2.add(id);
    }
    
    public List<Integer> getEntityIdsD1() {
        return entityIdsD1;
    }
    
    public List<Integer> getEntityIdsD2() {
        return entityIdsD2;
    }
    
    public void loadBulkEntityIdsD1(Collection<Integer> ids) {
        entityIdsD1.addAll(ids);
    }
}
