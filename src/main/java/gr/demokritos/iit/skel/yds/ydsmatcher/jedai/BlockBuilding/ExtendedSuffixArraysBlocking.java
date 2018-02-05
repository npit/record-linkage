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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

/**
 *
 * @author gap2
 */
public class ExtendedSuffixArraysBlocking extends SuffixArraysBlocking {

    private static final Logger LOGGER = Logger.getLogger(ExtendedSuffixArraysBlocking.class.getName());

    public ExtendedSuffixArraysBlocking() {
        this(39, 6);

        LOGGER.log(Level.INFO, "Using default configuration for {0}.", getMethodName());
    }

    public ExtendedSuffixArraysBlocking(int maxSize, int minLength) {
        super(maxSize, minLength);
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> suffixes = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            suffixes.addAll(getExtendedSuffixes(minimumSuffixLength, token));
        }
        return suffixes;
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it creates one block for every substring (not just suffix) that appears in the tokens of at least two entities.";
    }

    @Override
    public String getMethodName() {
        return "Extended Suffix Arrays Blocking";
    }

    public Set<String> getExtendedSuffixes(int minimumLength, String blockingKey) {
        final Set<String> suffixes = new HashSet<>();
        suffixes.add(blockingKey);
        if (minimumLength <= blockingKey.length()) {
            for (int nGramSize = blockingKey.length() - 1; minimumLength <= nGramSize; nGramSize--) {
                int currentPosition = 0;
                final int length = blockingKey.length() - (nGramSize - 1);
                while (currentPosition < length) {
                    String newSuffix = blockingKey.substring(currentPosition, currentPosition + nGramSize);
                    suffixes.add(newSuffix);
                    currentPosition++;
                }
            }
        }
        return suffixes;
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
        obj2.put("defaultValue", "39");
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
                return "The " + getParameterName(0) + " determines the minimum number of characters in an attribute value substring that is used as blocking key.";
            case 1:
                return "The " + getParameterName(1) + " determines the maximum number of entities that correspond to a valid substring (i.e., maximum block size).";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "Minimum Substring Length";
            case 1:
                return "Maximum Substring Frequency";
            default:
                return "invalid parameter id";
        }
    }
}
