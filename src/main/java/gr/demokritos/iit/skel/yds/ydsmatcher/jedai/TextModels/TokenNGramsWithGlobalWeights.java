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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class TokenNGramsWithGlobalWeights extends TokenNGrams {

    private static final Logger LOGGER = Logger.getLogger(TokenNGramsWithGlobalWeights.class.getName());

    protected final static Map<String, IncrementalCounter>[] DOC_FREQ = new HashMap[2];

    public TokenNGramsWithGlobalWeights(int did, int n, RepresentationModel model, SimilarityMetric simMetric, String iName) {
        super(did, n, model, simMetric, iName);
    }

    @Override
    public void finalizeModel() {
        if (DOC_FREQ[datasetId] == null) {
            DOC_FREQ[datasetId] = new HashMap<>();
        }

        for (String keyValue : itemsFrequency.keySet()) {
            IncrementalCounter frequency = DOC_FREQ[datasetId].get(keyValue);
            if (frequency == null) {
                frequency = new IncrementalCounter();
                DOC_FREQ[datasetId].put(keyValue, frequency);
            }
            frequency.incrementCounter();
        }
    }

    protected double getARCSSimilarity(TokenNGramsWithGlobalWeights oModel) {
        Set<String> commonKeys = new HashSet(itemsFrequency.keySet());
        commonKeys.retainAll(oModel.getItemsFrequency().keySet());

        double similarity = 0;
        if (datasetId == DATASET_1 && datasetId == oModel.getDatasetId()) { // Dirty ER
            for (String key : commonKeys) {
                double frequency = DOC_FREQ[DATASET_1].get(key).getCounter();
                similarity += 1.0 / (Math.log1p(frequency * (frequency - 1) / 2.0) / Math.log(2));
            }
        } else if (datasetId != oModel.getDatasetId()) { // Clean-Clean ER
            for (String key : commonKeys) {
                similarity += 1.0 / (Math.log1p(((double)DOC_FREQ[DATASET_1].get(key).getCounter()) * DOC_FREQ[DATASET_2].get(key).getCounter())/ Math.log(2));
            }
        } else {
            LOGGER.log(Level.SEVERE, "Both models come from dataset 1!");
            System.exit(-1);
        }

        return similarity;
    }

    protected double getIdfWeight(String keyValue) {
        IncrementalCounter frequency = DOC_FREQ[datasetId].get(keyValue);
        if (frequency == null) {
            return 0;
        }

        double weight = -0;
        if (NO_OF_DOCUMENTS[datasetId] < frequency.getCounter()) {
            LOGGER.log(Level.SEVERE, "Error in the computation of IDF weights!!!");
        } else if (frequency.getCounter() < NO_OF_DOCUMENTS[datasetId]) {
            weight = Math.log10(NO_OF_DOCUMENTS[datasetId] / (1 + frequency.getCounter()));
        }

        return weight;
    }

    protected double getSigmaSimilarity(TokenNGramsWithGlobalWeights oModel) {
        double totalTerms2 = oModel.getNoOfTotalTerms();
        Map<String, IncrementalCounter> itemVector2 = oModel.getItemsFrequency();

        double numerator = 0.0;
        for (Entry<String, IncrementalCounter> entry : itemsFrequency.entrySet()) {
            IncrementalCounter frequency2 = itemVector2.get(entry.getKey());
            if (frequency2 != null) {
                numerator += entry.getValue().getCounter() / noOfTotalTerms * getIdfWeight(entry.getKey())
                        + frequency2.getCounter() / totalTerms2 * oModel.getIdfWeight(entry.getKey());
            }
        }

        final Set<String> allKeys = new HashSet<>(itemsFrequency.keySet());
        allKeys.addAll(itemVector2.keySet());
        double denominator = 0.0;
        for (String key : allKeys) {
            IncrementalCounter frequency1 = itemsFrequency.get(key);
            IncrementalCounter frequency2 = itemVector2.get(key);
            double freq1 = frequency1 == null ? 0 : frequency1.getCounter() / noOfTotalTerms;
            double freq2 = frequency2 == null ? 0 : frequency2.getCounter() / totalTerms2;
            denominator += freq1 * getIdfWeight(key) + freq2 * oModel.getIdfWeight(key);
        }

        return numerator / denominator;
    }

    @Override
    public double getSimilarity(ITextModel oModel) {
        switch (simMetric) {
            case ARCS_SIMILARITY:
                return getARCSSimilarity((TokenNGramsWithGlobalWeights) oModel);
            case COSINE_SIMILARITY:
                return getTfIdfCosineSimilarity((TokenNGramsWithGlobalWeights) oModel);
            case GENERALIZED_JACCARD_SIMILARITY:
                return getTfIdfGeneralizedJaccardSimilarity((TokenNGramsWithGlobalWeights) oModel);
            case SIGMA_SIMILARITY:
                return getSigmaSimilarity((TokenNGramsWithGlobalWeights) oModel);
            default:
                LOGGER.log(Level.SEVERE, "The given similarity metric is incompatible with the bag representation model!");
                System.exit(-1);
                return -1;
        }
    }

    protected double getTfIdfCosineSimilarity(TokenNGramsWithGlobalWeights oModel) {
        double totalTerms2 = oModel.getNoOfTotalTerms();
        final Map<String, IncrementalCounter> otherItemVector = oModel.getItemsFrequency();

        double numerator = 0.0;
        for (Entry<String, IncrementalCounter> entry : itemsFrequency.entrySet()) {
            IncrementalCounter frequency2 = otherItemVector.get(entry.getKey());
            if (frequency2 != null) {
                numerator += (entry.getValue().getCounter() / noOfTotalTerms) * getIdfWeight(entry.getKey())
                        * (frequency2.getCounter() / totalTerms2) * oModel.getIdfWeight(entry.getKey());
            }
        }

        double denominator = getVectorMagnitude() * oModel.getVectorMagnitude();
        return numerator / denominator;
    }

    protected double getTfIdfGeneralizedJaccardSimilarity(TokenNGramsWithGlobalWeights oModel) {
        double totalTerms2 = oModel.getNoOfTotalTerms();
        Map<String, IncrementalCounter> itemVector2 = oModel.getItemsFrequency();

        double numerator = 0.0;
        for (Entry<String, IncrementalCounter> entry : itemsFrequency.entrySet()) {
            IncrementalCounter frequency2 = itemVector2.get(entry.getKey());
            if (frequency2 != null) {
                numerator += Math.min(entry.getValue().getCounter() / noOfTotalTerms * getIdfWeight(entry.getKey()),
                        frequency2.getCounter() / totalTerms2 * oModel.getIdfWeight(entry.getKey()));
            }
        }

        final Set<String> allKeys = new HashSet<>(itemsFrequency.keySet());
        allKeys.addAll(itemVector2.keySet());
        double denominator = 0.0;
        for (String key : allKeys) {
            IncrementalCounter frequency1 = itemsFrequency.get(key);
            IncrementalCounter frequency2 = itemVector2.get(key);
            double freq1 = frequency1 == null ? 0 : frequency1.getCounter() / noOfTotalTerms;
            double freq2 = frequency2 == null ? 0 : frequency2.getCounter() / totalTerms2;
            denominator += Math.max(freq1 * getIdfWeight(key), freq2 * oModel.getIdfWeight(key));
        }

        return numerator / denominator;
    }

    @Override
    protected double getVectorMagnitude() {
        double magnitude = 0.0;
        for (Entry<String, IncrementalCounter> entry : itemsFrequency.entrySet()) {
            magnitude += Math.pow(entry.getValue().getCounter() * getIdfWeight(entry.getKey()) / noOfTotalTerms, 2.0);
        }

        return Math.sqrt(magnitude);
    }
}
