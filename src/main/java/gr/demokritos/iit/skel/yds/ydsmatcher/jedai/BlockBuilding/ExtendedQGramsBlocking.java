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

import java.util.ArrayList;
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
public class ExtendedQGramsBlocking extends QGramsBlocking {

    private final static int MAX_Q_GRAMS = 15;
    private static final Logger LOGGER = Logger.getLogger(ExtendedQGramsBlocking.class.getName());

    private final double threshold;

    public ExtendedQGramsBlocking() {
        this(0.95, 6);

        LOGGER.log(Level.INFO, "Using default configuration for {0}.", getMethodName());
    }

    public ExtendedQGramsBlocking(double t, int n) {
        super(n);
        threshold = t;

        LOGGER.log(Level.INFO, getMethodConfiguration());
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> keys = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            List<String> nGrams = getNGrams(nGramSize, token);
            if (nGrams.size() == 1) {
                keys.add(nGrams.get(0));
            } else {
                if (MAX_Q_GRAMS < nGrams.size()) {
                    nGrams = nGrams.subList(0, MAX_Q_GRAMS);
                }

                int minimumLength = (int) Math.max(1, Math.floor(nGrams.size() * threshold));
                for (int i = minimumLength; i <= nGrams.size(); i++) {
                    keys.addAll(getCombinationsFor(nGrams, i));
                }
            }
        }
        return keys;
    }

    protected Set<String> getCombinationsFor(List<String> sublists, int sublistLength) {
        if (sublistLength == 0 || sublists.size() < sublistLength) {
            return new HashSet<>();
        }

        List<String> remainingElements = new ArrayList<>(sublists);
        String lastSublist = remainingElements.remove(sublists.size() - 1);

        final Set<String> combinationsExclusiveX = getCombinationsFor(remainingElements, sublistLength);
        final Set<String> combinationsInclusiveX = getCombinationsFor(remainingElements, sublistLength - 1);

        final Set<String> resultingCombinations = new HashSet<>();
        resultingCombinations.addAll(combinationsExclusiveX);
        if (combinationsInclusiveX.isEmpty()) {
            resultingCombinations.add(lastSublist);
        } else {
            combinationsInclusiveX.stream().forEach((combination) -> {
                resultingCombinations.add(combination + lastSublist);
            });
        }
        return resultingCombinations;
    }

    @Override
    public String getMethodConfiguration() {
        return getParameterName(0) + "=" + nGramSize + "\t"
                + getParameterName(1) + "=" + threshold;
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it creates one block for every combination of q-grams that represents at least two entities.\n"
                + "The q-grams are extracted from any token in the attribute values of any entity.";
    }

    @Override
    public String getMethodName() {
        return "Extended Q-Grams Blocking";
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
        obj2.put("class", "java.lang.Double");
        obj2.put("name", getParameterName(1));
        obj2.put("defaultValue", "0.95");
        obj2.put("minValue", "0.8");
        obj2.put("maxValue", "0.95");
        obj2.put("stepValue", "0.05");
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
                return "The " + getParameterName(0) + " defines the number of characters that comprise every q-gram.";
            case 1:
                return "The " + getParameterName(1) + " (t) defines the number N of q-grams that are combined to form an individual blocking key.\n"
                        + "In more detail, the minimum number l_{min} of q-grams per blocking key is defined as l_{min} = max (1, \\floor{k \\cdot t}),\n"
                        + "where k is the number of q-grams from the original blocking key (token).";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "Q-gram Size";
            case 1:
                return "Combination Threshold";
            default:
                return "invalid parameter id";
        }
    }
}
