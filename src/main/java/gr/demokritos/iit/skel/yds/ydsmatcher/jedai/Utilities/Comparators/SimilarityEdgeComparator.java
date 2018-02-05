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

import DataModel.SimilarityEdge;
import java.util.Comparator;

/**
 *
 * @author G.A.P. II
 */

public class SimilarityEdgeComparator implements Comparator<SimilarityEdge> {

    // orders edges from the highest weight/similarity to the lowest one
    
    @Override
    public int compare(SimilarityEdge se1,SimilarityEdge se2) {
        double test = se1.getSimilarity()-se2.getSimilarity(); 
        if (test > 0) {
            return -1;
        }

        if (test < 0) {
            return 1;
        }

        return 0;
    }
}