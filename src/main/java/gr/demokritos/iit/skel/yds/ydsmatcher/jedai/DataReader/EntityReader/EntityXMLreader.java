/*
* Copyright [2017] [George Papadakis (gpapadis@yahoo.gr)]
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
package DataReader.EntityReader;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import DataModel.EntityProfile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

/**
 *
 * @author G.A.P. II
 */
public class EntityXMLreader extends AbstractEntityReader {

    private static final Logger LOGGER = Logger.getLogger(EntityXMLreader.class.getName());

    private final Set<String> attributesToExclude;
    private final Map<String, EntityProfile> urlToEntity;

    public EntityXMLreader(String filePath) {
        super(filePath);
        attributesToExclude = new HashSet<>();
        urlToEntity = new HashMap<>();
    }

    @Override
    public List<EntityProfile> getEntityProfiles() {
        if (!entityProfiles.isEmpty()) {
            return entityProfiles;
        }

        if (inputFilePath == null) {
            LOGGER.log(Level.SEVERE, "Input file path has not been set!");
            return null;
        }

        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            Document document = saxBuilder.build(inputFilePath);
            readXMLdoc(document);
        } catch (JDOMException | IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }

        return entityProfiles;
    }

    @Override
    public String getMethodConfiguration() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        attributesToExclude.forEach((attributeName) -> {
            sb.append(attributeName).append(",");
        });
        sb.append("}");

        return getParameterName(0) + "=" + inputFilePath + "\t"
                + getParameterName(1) + "=" + sb.toString();
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it converts an XML file into a set of entity profiles.";
    }

    @Override
    public String getMethodName() {
        return "XML Reader";
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
        obj1.put("class", "java.lang.String");
        obj1.put("name", getParameterName(0));
        obj1.put("defaultValue", "-");
        obj1.put("minValue", "-");
        obj1.put("maxValue", "-");
        obj1.put("stepValue", "-");
        obj1.put("description", getParameterDescription(0));

        JsonObject obj2 = new JsonObject();
        obj2.put("class", "java.util.Set<String>");
        obj2.put("name", getParameterName(1));
        obj2.put("defaultValue", "-");
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
                return "The " + getParameterName(0) + " determines the absolute path to the XML file that will be read into main memory.";
            case 1:
                return "The " + getParameterName(1) + " specifies the attributes that will be ignored during the creation of entity profiles.";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "File Path";
            case 1:
                return "Attributes To Exclude";
            default:
                return "invalid parameter id";
        }
    }

    private void readXMLdoc(Document document) throws IOException {
        Element classElement = document.getRootElement();

        List<Element> dblpRoot = classElement.getChildren();
        for (int profCounter = 0; profCounter < dblpRoot.size(); profCounter++) {
            Element profile = dblpRoot.get(profCounter);
            
            String profName = profile.getName();
            EntityProfile entityProfile = urlToEntity.get(profName);
            if (entityProfile == null) {
                entityProfile = new EntityProfile(profName);
                urlToEntity.put(profName, entityProfile);
                entityProfiles.add(entityProfile);
            }
            
            List<Element> profAttributes = profile.getChildren();
            for (int attCounter = 0; attCounter < profAttributes.size(); attCounter++) {
                Element attr = profAttributes.get(attCounter);
                String attName = attr.getName();
                if (attributesToExclude.contains(attName)) {
                    continue;
                }
                String attValue = attr.getValue();
                entityProfile.addAttribute(attName, attValue);
            }
        }
    }

    public void setAttributesToExclude(String[] attributesNamesToExclude) {
        attributesToExclude.addAll(Arrays.asList(attributesNamesToExclude));
    }
}
