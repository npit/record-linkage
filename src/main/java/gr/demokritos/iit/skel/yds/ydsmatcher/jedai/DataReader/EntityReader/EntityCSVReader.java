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
package DataReader.EntityReader;

import DataModel.EntityProfile;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

/**
 *
 * @author G.A.P. II
 */
public class EntityCSVReader extends AbstractEntityReader {

    private static final Logger LOGGER = Logger.getLogger(EntityCSVReader.class.getName());

    private boolean attributeNamesInFirstRow;
    private char separator;
    private int idIndex;
    private String[] attributeNames; 
    private final Set<Integer> attributesToExclude;

    public EntityCSVReader(String filePath) {
        super(filePath);
        attributeNamesInFirstRow = false;
        attributeNames = null;
        idIndex = -1;
        separator = ',';
        attributesToExclude = new HashSet<>();
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

        try {
            //creating reader
            CSVReader reader = new CSVReader(new FileReader(inputFilePath), separator);

            //getting first line
            String[] firstLine = reader.readNext();
            int noOfAttributes = firstLine.length;
            if (noOfAttributes - 1 < idIndex) {
                LOGGER.log(Level.SEVERE, "Id index does not correspond to a valid column index! Counting starts from 0.");
                return null;
            }

            //setting attribute names
            int entityCounter = 0;
            if (attributeNamesInFirstRow) {
                attributeNames = Arrays.copyOf(firstLine, noOfAttributes);
            } else { // no attribute names in csv file
                attributeNames = new String[noOfAttributes];
                for (int i = 0; i < noOfAttributes; i++) {
                    attributeNames[i] = "attribute" + (i + 1);
                }

                entityCounter++; //first line corresponds to entity
                readEntity(entityCounter, firstLine);
            }

            //read entity profiles
            String[] nextLine = null;
            while ((nextLine = reader.readNext()) != null) {
                entityCounter++;

                if (nextLine.length < attributeNames.length - 1) {
                    LOGGER.log(Level.WARNING, "Line with missing attribute names : {0}", Arrays.toString(nextLine));
                    continue;
                }

                readEntity(entityCounter, nextLine);
            }

            return entityProfiles;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String getMethodConfiguration() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Integer attributeId : attributesToExclude) {
            sb.append(attributeId).append(",");
        }
        sb.append("}");

        return getParameterName(0) + "=" + inputFilePath + "\t"
                + getParameterName(1) + "=" + attributeNamesInFirstRow + "\t"
                + getParameterName(2) + "=" + separator + "\t"
                + getParameterName(3) + "=" + idIndex + "\t"
                + getParameterName(4) + "=" + sb.toString();
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it converts a CSV file into a set of entity profiles.";
    }

    @Override
    public String getMethodName() {
        return "CSV Reader";
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " involves five parameters:\n"
                + "1)" + getParameterDescription(0) + ".\n"
                + "2)" + getParameterDescription(1) + ".\n"
                + "3)" + getParameterDescription(2) + ".\n"
                + "4)" + getParameterDescription(3) + ".\n"
                + "5)" + getParameterDescription(4) + ".";
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
        obj2.put("class", "java.lang.Boolean");
        obj2.put("name", getParameterName(1));
        obj2.put("defaultValue", "true");
        obj2.put("minValue", "-");
        obj2.put("maxValue", "-");
        obj2.put("stepValue", "-");
        obj2.put("description", getParameterDescription(1));
        
        JsonObject obj3 = new JsonObject();
        obj3.put("class", "java.lang.Character");
        obj3.put("name", getParameterName(2));
        obj3.put("defaultValue", ",");
        obj3.put("minValue", "-");
        obj3.put("maxValue", "-");
        obj3.put("stepValue", "-");
        obj3.put("description", getParameterDescription(2));
        
        JsonObject obj4 = new JsonObject();
        obj4.put("class", "java.lang.Integer");
        obj4.put("name", getParameterName(3));
        obj4.put("defaultValue", "0");
        obj4.put("minValue", "0");
        obj4.put("maxValue", "-");
        obj4.put("stepValue", "-");
        obj4.put("description", getParameterDescription(3));
        
        JsonObject obj5 = new JsonObject();
        obj5.put("class", "java.util.Set<Integer>");
        obj5.put("name", getParameterName(4));
        obj5.put("defaultValue", "-");
        obj5.put("minValue", "-");
        obj5.put("maxValue", "-");
        obj5.put("stepValue", "-");
        obj5.put("description", getParameterDescription(4));

        JsonArray array = new JsonArray();
        array.add(obj1);
        array.add(obj2);
        array.add(obj3);
        array.add(obj4);
        array.add(obj5);
        return array;
    }

    @Override
    public String getParameterDescription(int parameterId) {
        switch (parameterId) {
            case 0:
                return "The " + getParameterName(0) + " determines the absolute path to the CSV file that will be read into main memory.";
            case 1:
                return "The " + getParameterName(1) + " determines whether the first line of the CSV file contains the attribute names (true), or it contains an entity profile (false).";
            case 2:
                return "The " + getParameterName(2) + " determines the character used to tokenize every line into attribute values.";
            case 3:
                return "The " + getParameterName(3) + " determines the number of column/attribute (starting from 0) that contains the entity ids. "
                       + "If the given id is larger than the number of columns, an exception is thrown. "
                       + "If id<0, an auto-incremented integer is assigned as id to every entity.";
            case 4:
                return "The " + getParameterName(4) + " specifies the column ids (in the form of comma-separated integers) that will be ignored during the creation of entity profiles.";
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
                return "Attribute Names In First Row";
            case 2:
                return "Separator";
            case 3:
                return "Id index";
            case 4:
                return "Attributes To Exclude";
            default:
                return "invalid parameter id";
        }
    }

    private void readEntity(int index, String[] currentLine) throws IOException {
        String entityId;
        if (idIndex < 0) {
            entityId = "id" + index;
        } else {
            entityId = currentLine[idIndex];
        }

        EntityProfile newProfile = new EntityProfile(entityId);
        for (int i = 0; i < currentLine.length; i++) {
            if (attributesToExclude.contains(i)) {
                continue;
            }
            if (!currentLine[i].isEmpty()) {
                newProfile.addAttribute(attributeNames[i], currentLine[i]);

            }
        }
        entityProfiles.add(newProfile);
    }

    public void setAttributesToExclude(int[] attributesIndicesToExclude) {
        for (int attributeIndex : attributesIndicesToExclude) {
            attributesToExclude.add(attributeIndex);
        }
    }

    public void setAttributeNamesInFirstRow(boolean attributeNamesInFirstRow) {
        this.attributeNamesInFirstRow = attributeNamesInFirstRow;
    }

    public void setIdIndex(int idIndex) {
        this.idIndex = idIndex;
        attributesToExclude.add(idIndex);
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }
}
