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

package Utilities.Comparators;

import DataModel.Comparison;
import java.util.Comparator;

/**
 *
 * @author G.A.P. II
 */

public class ComparisonWeightComparator implements Comparator<Comparison> {

    @Override
    public int compare(Comparison o1, Comparison o2) {
        double test = o2.getUtilityMeasure()-o1.getUtilityMeasure(); 
        if (0 < test) {
            return -1;
        }

        if (test < 0) {
            return 1;
        }

        return 0;
    }
    
}