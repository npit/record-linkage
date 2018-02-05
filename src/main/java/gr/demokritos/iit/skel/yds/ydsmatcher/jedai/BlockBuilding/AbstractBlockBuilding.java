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
import DataModel.Attribute;
import DataModel.BilateralBlock;
import DataModel.EntityProfile;
import DataModel.UnilateralBlock;
import Utilities.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */
public abstract class AbstractBlockBuilding implements IBlockBuilding {

    private static final Logger LOGGER = Logger.getLogger(AbstractBlockBuilding.class.getName());

    protected double noOfEntitiesD1;
    protected double noOfEntitiesD2;

    protected final List<AbstractBlock> blocks;
    protected List<EntityProfile> entityProfilesD1;
    protected List<EntityProfile> entityProfilesD2;
    protected Map<String, List<Integer>> invertedIndexD1;
    protected Map<String, List<Integer>> invertedIndexD2;

    public AbstractBlockBuilding() {
        blocks = new ArrayList<>();
        entityProfilesD1 = null;
        entityProfilesD2 = null;
    }

    protected void buildBlocks() {
        indexEntities(invertedIndexD1, entityProfilesD1);

        if (invertedIndexD2 != null) {
            indexEntities(invertedIndexD2, entityProfilesD2);
        }
    }

    protected abstract Set<String> getBlockingKeys(String attributeValue);

    @Override
    public List<AbstractBlock> getBlocks(List<EntityProfile> profiles) {
        return this.getBlocks(profiles, null);
    }

    @Override
    public List<AbstractBlock> getBlocks(List<EntityProfile> profilesD1,
            List<EntityProfile> profilesD2) {
        if (profilesD1 == null) {
            LOGGER.log(Level.SEVERE, "First list of entity profiles is null! "
                    + "The first argument should always contain entities.");
            return null;
        }

        invertedIndexD1 = new HashMap<>();
        entityProfilesD1 = profilesD1;
        noOfEntitiesD1 = entityProfilesD1.size();
        if (profilesD2 != null) {
            invertedIndexD2 = new HashMap<>();
            entityProfilesD2 = profilesD2;
            noOfEntitiesD2 = entityProfilesD2.size();
        }

        buildBlocks();
        return readBlocks();
    }

    public double getBruteForceComparisons() {
        if (entityProfilesD2 == null) {
            return noOfEntitiesD1 * (noOfEntitiesD1 - 1) / 2;
        }
        return noOfEntitiesD1 * noOfEntitiesD2;
    }

    public double getTotalNoOfEntities() {
        if (entityProfilesD2 == null) {
            return noOfEntitiesD1;
        }
        return noOfEntitiesD1 + noOfEntitiesD2;
    }

    protected void indexEntities(Map<String, List<Integer>> index, List<EntityProfile> entities) {
        int counter = 0;
        for (EntityProfile profile : entities) {
            for (Attribute attribute : profile.getAttributes()) {
                Set<String> keys = getBlockingKeys(attribute.getValue());
                for (String key : keys) {
                    String normalizedKey = key.trim().toLowerCase();
                    if (0 < normalizedKey.length()) {
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

    protected Map<String, int[]> parseD1Index() {
        final Map<String, int[]> hashedBlocks = new HashMap<>();
        invertedIndexD1.entrySet().stream().filter((entry) -> !(!invertedIndexD2.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            // check whether it is a common term
            int[] idsArray = Converter.convertCollectionToArray(entry.getValue());
            hashedBlocks.put(entry.getKey(), idsArray);
        });
        return hashedBlocks;
    }

    protected void parseD2Index(Map<String, int[]> hashedBlocks) {
        invertedIndexD2.entrySet().stream().filter((entry) -> !(!hashedBlocks.containsKey(entry.getKey()))).forEachOrdered((entry) -> {
            int[] idsArray = Converter.convertCollectionToArray(entry.getValue());
            int[] d1Entities = hashedBlocks.get(entry.getKey());
            blocks.add(new BilateralBlock(d1Entities, idsArray));
        });
    }

    protected void parseIndex() {
        invertedIndexD1.values().stream().filter((entityList) -> (1 < entityList.size())).map((entityList) -> Converter.convertCollectionToArray(entityList)).map((idsArray) -> new UnilateralBlock(idsArray)).forEachOrdered((block) -> {
            blocks.add(block);
        });
    }

    //read blocks from Lucene index
    public List<AbstractBlock> readBlocks() {
        if (entityProfilesD2 == null) { //Dirty ER
            parseIndex();
        } else {
            Map<String, int[]> hashedBlocks = parseD1Index();
            parseD2Index(hashedBlocks);
        }

        return blocks;
    }
}
