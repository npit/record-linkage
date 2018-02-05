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

import DataModel.AbstractBlock;
import DataModel.BilateralBlock;
import DataModel.UnilateralBlock;
import Utilities.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

/**
 *
 * @author gap2
 */
public class SortedNeighborhoodBlocking extends StandardBlocking {

    private static final Logger LOGGER = Logger.getLogger(SortedNeighborhoodBlocking.class.getName());

    protected final int windowSize;

    public SortedNeighborhoodBlocking() {
        this(4);
        
        LOGGER.log(Level.INFO, "Using default configuration for {0}.", getMethodName());
    }

    public SortedNeighborhoodBlocking(int w) {
        super();
        windowSize = w;
        
        LOGGER.log(Level.INFO, getMethodConfiguration());
    }

    @Override
    public String getMethodConfiguration() {
        return "Window size=" + windowSize;
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it creates blocks based on the similarity of the blocking keys of Standard Blocking:\n"
                + "it sorts the keys in alphabetical order, it sorts the entities accordingly and then, it slides a window over the sorted list of entities;\n"
                + "the entities that co-occur inside the window in every iteration form a block and are compared with each other.";
    }

    @Override
    public String getMethodName() {
        return "Sorted Neighborhood Blocking";
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " involves a single parameter:\n"
               + "1)" + getParameterDescription(0) + ".\n";
    }

    protected Integer[] getMixedSortedEntities(String[] sortedTerms) {
        int datasetLimit = entityProfilesD1.size();
        final List<Integer> sortedEntityIds = new ArrayList<>();

        for (String blockingKey : sortedTerms) {
            List<Integer> sortedIds = null;
            if (!invertedIndexD1.containsKey(blockingKey)) {
                sortedIds = new ArrayList<>();
            } else {
                sortedIds = new ArrayList<>(invertedIndexD1.get(blockingKey));
            }
            
            if (invertedIndexD2.containsKey(blockingKey)) {
                List<Integer> d2EntityIds = invertedIndexD2.get(blockingKey);
                for (Integer entityId : d2EntityIds) {
                    sortedIds.add(datasetLimit + entityId);
                }
            }

            Collections.shuffle(sortedIds);
            sortedEntityIds.addAll(sortedIds);
        }

        return sortedEntityIds.toArray(new Integer[sortedEntityIds.size()]);
    }

    @Override
    public JsonArray getParameterConfiguration() {
        JsonObject obj1 = new JsonObject();
        obj1.put("class", "java.lang.Integer");
        obj1.put("name", getParameterName(0));
        obj1.put("defaultValue", "2");
        obj1.put("minValue", "2");
        obj1.put("maxValue", "100");
        obj1.put("stepValue", "1");
        obj1.put("description", getParameterDescription(0));

        JsonArray array = new JsonArray();
        array.add(obj1);
        return array;
    }

    @Override
    public String getParameterDescription(int parameterId) {
        switch (parameterId) {
            case 0:
                return "The " + getParameterName(0) + " determines the fixed size of the window that slides over the sorted list of blocking keys.";
            default:
                return "invalid parameter id";
        }
    }
    
    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "Window Size";
            default:
                return "invalid parameter id";
        }
    }
    
    protected Integer[] getSortedEntities(String[] sortedTerms) {
        final List<Integer> sortedEntityIds = new ArrayList<>();

        for (String blockingKey : sortedTerms) {
            List<Integer> sortedIds = invertedIndexD1.get(blockingKey);
            Collections.shuffle(sortedIds);
            sortedEntityIds.addAll(sortedIds);
        }

        return sortedEntityIds.toArray(new Integer[sortedEntityIds.size()]);
    }

    @Override
    protected void parseIndex() {
        final Set<String> blockingKeysSet = invertedIndexD1.keySet();
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        Integer[] allEntityIds = getSortedEntities(sortedTerms);

        //slide window over the sorted list of entity ids
        int upperLimit = allEntityIds.length - windowSize;
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                entityIds.add(allEntityIds[i + j]);
            }

            if (1 < entityIds.size()) {
                int[] idsArray = Converter.convertCollectionToArray(entityIds);
                UnilateralBlock uBlock = new UnilateralBlock(idsArray);
                blocks.add(uBlock);
            }
        }
    }

    protected void parseIndices() {
        final Set<String> blockingKeysSet = new HashSet<>();
        blockingKeysSet.addAll(invertedIndexD1.keySet());
        blockingKeysSet.addAll(invertedIndexD2.keySet());
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        Integer[] allEntityIds = getMixedSortedEntities(sortedTerms);

        int datasetLimit = entityProfilesD1.size();
        //slide window over the sorted list of entity ids
        int upperLimit = allEntityIds.length - windowSize;
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds1 = new HashSet<>();
            final Set<Integer> entityIds2 = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                if (allEntityIds[i + j] < datasetLimit) {
                    entityIds1.add(allEntityIds[i + j]);
                } else {
                    entityIds2.add(allEntityIds[i + j] - datasetLimit);
                }
            }

            if (!entityIds1.isEmpty() && !entityIds2.isEmpty()) {
                int[] idsArray1 = Converter.convertCollectionToArray(entityIds1);
                int[] idsArray2 = Converter.convertCollectionToArray(entityIds2);
                BilateralBlock bBlock = new BilateralBlock(idsArray1, idsArray2);
                blocks.add(bBlock);
            }
        }
    }

    @Override
    public List<AbstractBlock> readBlocks() {
        if (entityProfilesD2 == null) { //Dirty ER
            parseIndex();
        } else {
            parseIndices();
        }

        return blocks;
    }
}
