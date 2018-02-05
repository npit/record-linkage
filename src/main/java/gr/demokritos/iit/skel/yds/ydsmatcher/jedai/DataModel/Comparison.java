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

/**
 *
 * @author G.A.P. II
 */

public class Comparison implements Serializable {

    private static final long serialVersionUID = 723425435776147L;

    private final boolean cleanCleanER;
    private final int entityId1;
    private final int entityId2;
    private double utilityMeasure;

    public Comparison (boolean ccER, int id1, int id2) {
        cleanCleanER = ccER;
        entityId1 = id1;
        entityId2 = id2;
        utilityMeasure = -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Comparison other = (Comparison) obj;
        if (this.entityId1 != other.getEntityId1()) {
            return false;
        }
        if (this.entityId2 != other.getEntityId2()) {
            return false;
        }
        return true;
    }
    
    public int getEntityId1() {
        return entityId1;
    }

    public int getEntityId2() {
        return entityId2;
    }
    
    public double getUtilityMeasure() {
        return utilityMeasure;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + this.entityId1;
        hash = 61 * hash + this.entityId2;
        return hash;
    }
    
    public boolean isCleanCleanER() {
        return cleanCleanER;
    }
    
    public void setUtilityMeasure(double utilityMeasure) {
        this.utilityMeasure = utilityMeasure;
    }
}