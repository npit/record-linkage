/*
* Copyright [2016-2017] [George Papadakis (gpapadis@yahoo.gr)]
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

package Utilities.Enumerations;

import TextModels.AbstractModel;
import TextModels.CharacterNGramGraphs;
import TextModels.CharacterNGrams;
import TextModels.TokenNGramGraphs;
import TextModels.TokenNGrams;
import TextModels.TokenNGramsWithGlobalWeights;

/**
 *
 * @author G.A.P. II
 */

public enum RepresentationModel {
    CHARACTER_BIGRAMS,
    CHARACTER_BIGRAM_GRAPHS,
    CHARACTER_TRIGRAMS,
    CHARACTER_TRIGRAM_GRAPHS,
    CHARACTER_FOURGRAMS,
    CHARACTER_FOURGRAM_GRAPHS,
    TOKEN_UNIGRAMS, 
    TOKEN_UNIGRAMS_TF_IDF, 
    TOKEN_UNIGRAM_GRAPHS, 
    TOKEN_BIGRAMS,
    TOKEN_BIGRAMS_TF_IDF, 
    TOKEN_BIGRAM_GRAPHS, 
    TOKEN_TRIGRAMS,
    TOKEN_TRIGRAMS_TF_IDF, 
    TOKEN_TRIGRAM_GRAPHS;
    
    public static AbstractModel getModel (int dId, RepresentationModel model, SimilarityMetric simMetric, String instanceName) {
        switch (model) {
            case CHARACTER_BIGRAMS:
                return new CharacterNGrams(dId, 2, model, simMetric, instanceName);
            case CHARACTER_BIGRAM_GRAPHS:
                return new CharacterNGramGraphs(dId, 2, model, simMetric, instanceName);
            case CHARACTER_FOURGRAMS:
                return new CharacterNGrams(dId, 4, model, simMetric, instanceName);
            case CHARACTER_FOURGRAM_GRAPHS:
                return new CharacterNGramGraphs(dId, 4, model, simMetric, instanceName);
            case CHARACTER_TRIGRAMS:
                return new CharacterNGrams(dId, 3, model, simMetric, instanceName);
            case CHARACTER_TRIGRAM_GRAPHS:
                return new CharacterNGramGraphs(dId, 3, model, simMetric, instanceName);
            case TOKEN_BIGRAMS:
                return new TokenNGrams(dId, 2, model, simMetric, instanceName);
            case TOKEN_BIGRAMS_TF_IDF:
                return new TokenNGramsWithGlobalWeights(dId, 2, model, simMetric, instanceName);
            case TOKEN_BIGRAM_GRAPHS:
                return new TokenNGramGraphs(dId, 2, model, simMetric, instanceName);
            case TOKEN_TRIGRAMS:
                return new TokenNGrams(dId, 3, model, simMetric, instanceName);
            case TOKEN_TRIGRAMS_TF_IDF:
                return new TokenNGramsWithGlobalWeights(dId, 3, model, simMetric, instanceName);
            case TOKEN_TRIGRAM_GRAPHS:
                return new TokenNGramGraphs(dId, 3, model, simMetric, instanceName);
            case TOKEN_UNIGRAMS:
                return new TokenNGrams(dId, 1, model, simMetric, instanceName);
            case TOKEN_UNIGRAMS_TF_IDF:
                return new TokenNGramsWithGlobalWeights(dId, 1, model, simMetric, instanceName);
            case TOKEN_UNIGRAM_GRAPHS:
                return new TokenNGramGraphs(dId, 1, model, simMetric, instanceName);
            default:
                return null;    
        }
    }
}