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
package TextModels;

import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */
public abstract class GraphModel extends AbstractModel {

    private static final Logger LOGGER = Logger.getLogger(GraphModel.class.getName());

    protected DocumentNGramGraph graphModel;
    protected final static NGramCachedGraphComparator COMPARATOR = new NGramCachedGraphComparator();

    public GraphModel(int dId, int n, RepresentationModel model, SimilarityMetric simMetric, String iName) {
        super(dId, n, model, simMetric, iName);
    }

    @Override
    public void finalizeModel() {};
    
    protected DocumentNGramGraph getGraphModel() {
        return graphModel;
    }

    @Override
    public double getSimilarity(ITextModel oModel) {
        final GraphSimilarity graphSimilarity = COMPARATOR.getSimilarityBetween(this.getGraphModel(), ((GraphModel) oModel).getGraphModel());
        switch (simMetric) {
            case GRAPH_CONTAINMENT_SIMILARITY:
                return graphSimilarity.ContainmentSimilarity;
            case GRAPH_NORMALIZED_VALUE_SIMILARITY:
                if (0 < graphSimilarity.SizeSimilarity) {
                    return graphSimilarity.ValueSimilarity / graphSimilarity.SizeSimilarity;
                }
            case GRAPH_VALUE_SIMILARITY:
                return graphSimilarity.ValueSimilarity;
            case GRAPH_OVERALL_SIMILARITY:
                double overallSimilarity = graphSimilarity.ContainmentSimilarity;
                overallSimilarity += graphSimilarity.ValueSimilarity;
                if (0 < graphSimilarity.SizeSimilarity) {
                    overallSimilarity += graphSimilarity.ValueSimilarity / graphSimilarity.SizeSimilarity;
                    return overallSimilarity / 3;
                }
                return overallSimilarity / 2;
            default:
                LOGGER.log(Level.SEVERE, "The given similarity metric is incompatible with the n-gram graphs representation model!");
                System.exit(-1);
                return -1;
        }
    }
}
