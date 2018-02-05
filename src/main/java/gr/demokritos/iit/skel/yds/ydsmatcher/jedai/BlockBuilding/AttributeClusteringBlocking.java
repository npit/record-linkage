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
package BlockBuilding;

import DataModel.Attribute;
import DataModel.EntityProfile;
import TextModels.ITextModel;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author gap2
 */
public class AttributeClusteringBlocking extends StandardBlocking {

    private final static double MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD = 1E-11;
    private final static String CLUSTER_PREFIX = "#$!cl";
    private final static String CLUSTER_SUFFIX = "!$#";
    private static final Logger LOGGER = Logger.getLogger(AttributeClusteringBlocking.class.getName());

    private final Map<String, Integer>[] attributeClusters;
    private final RepresentationModel model;
    private final SimilarityMetric simMetric;

    public AttributeClusteringBlocking() {
        this(RepresentationModel.TOKEN_UNIGRAM_GRAPHS, SimilarityMetric.GRAPH_VALUE_SIMILARITY);

        LOGGER.log(Level.INFO, "Using default configuration for {0}.", getMethodName());
    }

    public AttributeClusteringBlocking(RepresentationModel md, SimilarityMetric sMetric) {
        super();
        model = md;
        simMetric = sMetric;
        attributeClusters = new HashMap[2];

        LOGGER.log(Level.INFO, getMethodConfiguration());
    }

    @Override
    protected void buildBlocks() {
        ITextModel[] attributeModels1 = buildAttributeModels(DATASET_1, entityProfilesD1);
        if (entityProfilesD2 != null) {
            ITextModel[] attributeModels2 = buildAttributeModels(DATASET_2, entityProfilesD2);
            SimpleGraph graph = compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }

        indexEntities(0, invertedIndexD1, entityProfilesD1);
        if (invertedIndexD2 != null) {
            indexEntities(1, invertedIndexD2, entityProfilesD2);
        }
    }

    private ITextModel[] buildAttributeModels(int datasetId, List<EntityProfile> profiles) {
        final Map<String, List<String>> attributeProfiles = new HashMap<>();
        profiles.forEach((entity) -> {
            entity.getAttributes().forEach((attribute) -> {
                List<String> values = attributeProfiles.get(attribute.getName());
                if (values == null) {
                    values = new ArrayList<>();
                    attributeProfiles.put(attribute.getName(), values);
                }
                values.add(attribute.getValue().toLowerCase());
            });
        });

        int index = 0;
        ITextModel[] attributeModels = new ITextModel[attributeProfiles.size()];
        for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
            attributeModels[index] = RepresentationModel.getModel(datasetId, model, simMetric, entry.getKey());
            for (String value : entry.getValue()) {
                attributeModels[index].updateModel(value);
            }
            index++;
        }
        return attributeModels;
    }

    private void clusterAttributes(ITextModel[] attributeModels, SimpleGraph graph) {
        int noOfAttributes = attributeModels.length;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;

        attributeClusters[0] = new HashMap<>(2 * noOfAttributes);
        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            int clusterId = counter;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                counter++;
            }

            for (int attributeId : cluster) {
                attributeClusters[0].put(attributeModels[attributeId].getInstanceName(), clusterId);
            }
        }
        attributeClusters[1] = null;
    }

    private void clusterAttributes(ITextModel[] attributeModels1, ITextModel[] attributeModels2, SimpleGraph graph) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;

        attributeClusters[0] = new HashMap<>(2 * d1Attributes);
        attributeClusters[1] = new HashMap<>(2 * d2Attributes);
        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            int clusterId = counter;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                counter++;
            }

            for (int attributeId : cluster) {
                if (attributeId < d1Attributes) {
                    attributeClusters[0].put(attributeModels1[attributeId].getInstanceName(), clusterId);
                } else {
                    attributeClusters[1].put(attributeModels2[attributeId - d1Attributes].getInstanceName(), clusterId);
                }
            }
        }
    }

    private SimpleGraph compareAttributes(ITextModel[] attributeModels) {
        int noOfAttributes = attributeModels.length;
        int[] mostSimilarName = new int[noOfAttributes];
        double[] maxSimillarity = new double[noOfAttributes];
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < noOfAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        for (int i = 0; i < noOfAttributes; i++) {
            for (int j = i + 1; j < noOfAttributes; j++) {
                double simValue = attributeModels[i].getSimilarity(attributeModels[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j;
                }

                if (maxSimillarity[j] < simValue) {
                    maxSimillarity[j] = simValue;
                    mostSimilarName[j] = i;
                }
            }
        }

        for (int i = 0; i < noOfAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    private SimpleGraph compareAttributes(ITextModel[] attributeModels1, ITextModel[] attributeModels2) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;
        int totalAttributes = d1Attributes + d2Attributes;
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);

        int[] mostSimilarName = new int[totalAttributes];
        double[] maxSimillarity = new double[totalAttributes];
        for (int i = 0; i < totalAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        for (int i = 0; i < d1Attributes; i++) {
            for (int j = 0; j < d2Attributes; j++) {
                double simValue = attributeModels1[i].getSimilarity(attributeModels2[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j + d1Attributes;
                }

                if (maxSimillarity[j + d1Attributes] < simValue) {
                    maxSimillarity[j + d1Attributes] = simValue;
                    mostSimilarName[j + d1Attributes] = i;
                }
            }
        }

        for (int i = 0; i < totalAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    protected void indexEntities(int sourceId, Map<String, List<Integer>> index, List<EntityProfile> entities) {
        int counter = 0;
        for (EntityProfile profile : entities) {
            for (Attribute attribute : profile.getAttributes()) {
                Integer clusterId = attributeClusters[sourceId].get(attribute.getName());
                if (clusterId == null) {
                    LOGGER.log(Level.WARNING, "No cluster id found for attribute name\t:\t{0}"
                            + ".\nCorresponding attribute value\t:\t{1}", new Object[]{attribute.getName(), attribute.getValue()});
                    continue;
                }

                String clusterSuffix = CLUSTER_PREFIX + clusterId + CLUSTER_SUFFIX;
                for (String token : getTokens(attribute.getValue())) {
                    if (0 < token.trim().length()) {
                        String normalizedKey = token.trim().toLowerCase() + clusterSuffix;
                        List<Integer> entityList = index.get(normalizedKey);
                        if (entityList == null) {
                            entityList = new ArrayList<>();
                            index.put(normalizedKey, entityList);
                        }
                        entityList.add(counter);
                    }
                }
            }
            counter++;
        }
    }

    @Override
    public String getMethodConfiguration() {
        return getParameterName(0) + "=" + model + "\t"
                + getParameterName(1) + "=" + simMetric;
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it groups the attribute names into similarity clusters "
                + "and applies Standard Blocking to the values of every cluster, independently of the others.";
    }

    @Override
    public String getMethodName() {
        return "Attribute Clustering Blocking";
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " involves two parameters:\n"
                + "1)" + getParameterDescription(0) + ".\n"
                + "2)" + getParameterDescription(1) + ".";
    }

    @Override
    public JsonArray getParameterConfiguration() {
        JsonObject obj1 = new JsonObject();
        obj1.put("class", "Utilities.Enumerations.RepresentationModel");
        obj1.put("name", getParameterName(0));
        obj1.put("defaultValue", "Utilities.Enumerations.RepresentationModel.TOKEN_UNIGRAM_GRAPHS");
        obj1.put("minValue", "-");
        obj1.put("maxValue", "-");
        obj1.put("stepValue", "-");
        obj1.put("description", getParameterDescription(0));

        JsonObject obj2 = new JsonObject();
        obj2.put("class", "Utilities.Enumerations.SimilarityMetric");
        obj2.put("name", getParameterName(1));
        obj2.put("defaultValue", "Utilities.Enumerations.SimilarityMetric.GRAPH_VALUE_SIMILARITY");
        obj2.put("minValue", "-");
        obj2.put("maxValue", "-");
        obj2.put("stepValue", "-");
        obj2.put("description", getParameterDescription(1));

        JsonArray array = new JsonArray();
        array.add(obj1);
        array.add(obj2);
        return array;
    }

    @Override
    public String getParameterDescription(int parameterId) {
        switch (parameterId) {
            case 0:
                return "The " + getParameterName(0) + " is a character- or token-based bag or graph model that aggregates tall attribute values in an entity profile.";
            case 1:
                return "The " + getParameterName(1) + " is a bag or graph similarity metric that compares the models of two entity profiles, returning a value between 0 (completely dissimlar) and 1 (identical).";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "Representation Model";
            case 1:
                return "Similarity Measure";
            default:
                return "invalid parameter id";
        }
    }
}
