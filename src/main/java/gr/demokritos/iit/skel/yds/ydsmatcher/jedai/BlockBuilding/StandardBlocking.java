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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.atlas.json.JsonArray;

/**
 *
 * @author gap2
 */
public class StandardBlocking extends AbstractBlockBuilding {

    private static final Logger LOGGER = Logger.getLogger(StandardBlocking.class.getName());
    
    public StandardBlocking() {
        super();
        
        LOGGER.log(Level.INFO, "{0} initiated", getMethodName());
    }
    
    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        return new HashSet<>(Arrays.asList(getTokens(attributeValue)));
    }
    
    protected String[] getTokens (String attributeValue) {
        return attributeValue.split("[\\W_]");
    }

    @Override
    public String getMethodConfiguration() {
        return PARAMETER_FREE;
    }
    
    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it creates one block for every token in the attribute values of at least two entities.";
    }

    @Override
    public String getMethodName() {
        return "Standard Blocking";
    }
    
    @Override
    public String getMethodParameters() {
        return getMethodName() + " is a " + PARAMETER_FREE + ", as it uses unsupervised, schema-agnostic blocking keys:\n"
                + "every token is a blocking key.";
    }

    @Override
    public JsonArray getParameterConfiguration() {
        return new JsonArray();
    }

    @Override
    public String getParameterDescription(int parameterId) {
        return PARAMETER_FREE;
    }

    @Override
    public String getParameterName(int parameterId) {
        return PARAMETER_FREE;
    }
}