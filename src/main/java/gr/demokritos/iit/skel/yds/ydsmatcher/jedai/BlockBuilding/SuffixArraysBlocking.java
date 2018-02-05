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
package BlockBuilding;

import DataModel.AbstractBlock;
import DataModel.EntityProfile;

import java.util.HashSet;
import java.util.Iterator;
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
public class SuffixArraysBlocking extends StandardBlocking {

    private static final Logger LOGGER = Logger.getLogger(SuffixArraysBlocking.class.getName());

    protected final int maximumBlockSize;
    protected final int minimumSuffixLength;

    public SuffixArraysBlocking() {
        this(53, 6);

        LOGGER.log(Level.INFO, "Using default configuration for {0}.", getMethodName());
    }

    public SuffixArraysBlocking(int maxSize, int minLength) {
        super();
        maximumBlockSize = maxSize;
        minimumSuffixLength = minLength;

        LOGGER.log(Level.INFO, getMethodConfiguration());
    }

    @Override
    public List<AbstractBlock> getBlocks(List<EntityProfile> profilesD1,
            List<EntityProfile> profilesD2) {
        List<AbstractBlock> purgedBlocks = super.getBlocks(profilesD1, profilesD2);
        Iterator<AbstractBlock> blocksIterator = purgedBlocks.iterator();
        while (blocksIterator.hasNext()) {
            AbstractBlock block = (AbstractBlock) blocksIterator.next();
            if (maximumBlockSize < block.getTotalBlockAssignments()) {
                blocksIterator.remove();
            }
        }
        return purgedBlocks;
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> suffixes = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            suffixes.addAll(getSuffixes(minimumSuffixLength, token));
        }
        return suffixes;
    }

    @Override
    public String getMethodConfiguration() {
        return getParameterName(0) + "=" + maximumBlockSize + "\t"
                + getParameterName(1) + "=" + minimumSuffixLength;
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it creates one block for every suffix that appears in the attribute value tokens of at least two entities.";
    }

    @Override
    public String getMethodName() {
        return "Suffix Arrays Blocking";
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
        obj1.put("class", "java.lang.Integer");
        obj1.put("name", getParameterName(0));
        obj1.put("defaultValue", "6");
        obj1.put("minValue", "2");
        obj1.put("maxValue", "6");
        obj1.put("stepValue", "1");
        obj1.put("description", getParameterDescription(0));

        JsonObject obj2 = new JsonObject();
        obj2.put("class", "java.lang.Integer");
        obj2.put("name", getParameterName(1));
        obj2.put("defaultValue", "53");
        obj2.put("minValue", "2");
        obj2.put("maxValue", "100");
        obj2.put("stepValue", "1");
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
                return "The " + getParameterName(0) + " determines the minimum number of characters in a suffix that is used as blocking key.";
            case 1:
                return "The " + getParameterName(1) + " determines the maximum number of entities that correspond to a valid suffix (i.e., maximum block size).";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "Minimum Suffix Length";
            case 1:
                return "Maximum Suffix Frequency";
            default:
                return "invalid parameter id";
        }
    }

    public Set<String> getSuffixes(int minimumLength, String blockingKey) {
        final Set<String> suffixes = new HashSet<>();
        if (blockingKey.length() < minimumLength) {
            suffixes.add(blockingKey);
        } else {
            int limit = blockingKey.length() - minimumLength + 1;
            for (int i = 0; i < limit; i++) {
                suffixes.add(blockingKey.substring(i));
            }
        }
        return suffixes;
    }
}
