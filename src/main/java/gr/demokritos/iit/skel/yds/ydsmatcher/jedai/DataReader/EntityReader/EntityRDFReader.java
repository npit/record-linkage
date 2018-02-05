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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;

/**
 *
 * @author G.A.P. II
 */
public class EntityRDFReader extends AbstractEntityReader {

    private static final Logger LOGGER = Logger.getLogger(EntityRDFReader.class.getName());

    private final Set<String> attributesToExclude;
    private final Map<String, EntityProfile> urlToEntity;

    public EntityRDFReader(String filePath) {
        super(filePath);
        
        urlToEntity = new HashMap<>();
        attributesToExclude = new HashSet<>();
        attributesToExclude.add("owl:sameAs");
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

        //load the rdf model from the input file
        try {
            Model model = RDFDataMgr.loadModel(inputFilePath);
            readModel(model);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
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
        return getMethodName() + ": it converts an RDF file of any format into a set of entity profiles.";
    }

    @Override
    public String getMethodName() {
        return "RDF Reader";
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
                return "The " + getParameterName(0) + " determines the absolute path to the RDF file that will be read into main memory.";
            case 1:
                return "The " + getParameterName(1) + " specifies the predicates that will be ignored during the creation of entity profiles.";
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
                return "Predicates To Exclude";
            default:
                return "invalid parameter id";
        }
    }

    private void readModel(Model m) throws IOException {
        //read each ntriples
        //get spo, create a separate profile for each separate subject,
        //with Attribute=predicate and Value=object
        StmtIterator iter = m.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();

            Property predicate = stmt.getPredicate();
            String pred = predicate.toString();
            if (attributesToExclude.contains(pred)) {
                continue;
            }

            Resource subject = stmt.getSubject();
            String sub = subject.toString();

            RDFNode object = stmt.getObject();
            String obj = object.toString();

            //if already exists a profile for the subject, simply add po as <Att>-<Value>
            EntityProfile entityProfile = urlToEntity.get(sub);
            if (entityProfile == null) {
                entityProfile = new EntityProfile(sub);
                entityProfiles.add(entityProfile);
                urlToEntity.put(sub, entityProfile);
            }

            if (!obj.isEmpty()) {
                entityProfile.addAttribute(pred, obj);
            }
        }
    }

    public void setAttributesToExclude(String[] attributesNamesToExclude) {
        attributesToExclude.addAll(Arrays.asList(attributesNamesToExclude));
    }
}
