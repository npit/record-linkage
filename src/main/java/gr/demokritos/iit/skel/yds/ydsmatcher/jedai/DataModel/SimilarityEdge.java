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


/**
 *
 * @author G.A.P. II
 */

public class SimilarityEdge {

    private final int model1pos;
    private final int model2pos;
    private final double similarity;

    public SimilarityEdge (int pos1, int pos2, double sim) {
    	model1pos = pos1;
    	model2pos = pos2;
    	similarity = sim;
    }

    public int getModel1Pos() {
        return model1pos;
    }
    
    public int getModel2Pos() {
        return model2pos;
    }

    public double getSimilarity() {
        return similarity;
    }
    
    
    @Override
    public String toString() {
        return "pos1\t:\t" + model1pos + ", pos2\t:\t" + model2pos +", similarity\t:\t" + similarity;
    }
}