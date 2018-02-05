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
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class EndpointReader extends AbstractEntityReader {

    private static final Logger LOGGER = Logger.getLogger(EndpointReader.class.getName());

    private String password;
    private String user;

    private final Set<String> attributesToExclude;
    private final Map<String, EntityProfile> urlToEntity;

    public EndpointReader(String endpointUrl) {
        super(endpointUrl);

        password = null;
        user = null;

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
            readEndpoint(inputFilePath);
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
        for (String attributeName : attributesToExclude) {
            sb.append(attributeName).append(",");
        }
        sb.append("}");
    
        return getParameterName(0) + "=" + inputFilePath + "\t"
               + getParameterName(1) + "=" + user + "\t"
               + getParameterName(2) + "=" + password + "\t"
               + getParameterName(3) + "=" + sb.toString();
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it converts a SPARQL endpoint into a set of entity profiles.";
    }

    @Override
    public String getMethodName() {
        return "SPARQL Endpoint Reader";
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " involves four parameters:\n"
                + "1)" + getParameterDescription(0) + ".\n"
                + "2)" + getParameterDescription(1) + ".\n"
                + "3)" + getParameterDescription(2) + ".\n"
                + "4)" + getParameterDescription(3) + ".";
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
        obj2.put("class", "java.lang.String");
        obj2.put("name", getParameterName(1));
        obj2.put("defaultValue", "-");
        obj2.put("minValue", "-");
        obj2.put("maxValue", "-");
        obj2.put("stepValue", "-");
        obj2.put("description", getParameterDescription(1));
        
        JsonObject obj3 = new JsonObject();
        obj3.put("class", "java.lang.String");
        obj3.put("name", getParameterName(2));
        obj3.put("defaultValue", "-");
        obj3.put("minValue", "-");
        obj3.put("maxValue", "-");
        obj3.put("stepValue", "-");
        obj3.put("description", getParameterDescription(2));
                
        JsonObject obj4 = new JsonObject();
        obj4.put("class", "java.util.Set<String>");
        obj4.put("name", getParameterName(3));
        obj4.put("defaultValue", "-");
        obj4.put("minValue", "-");
        obj4.put("maxValue", "-");
        obj4.put("stepValue", "-");
        obj4.put("description", getParameterDescription(3));

        JsonArray array = new JsonArray();
        array.add(obj1);
        array.add(obj2);
        array.add(obj3);
        array.add(obj4);
        return array;
    }

    @Override
    public String getParameterDescription(int parameterId) {
        switch (parameterId) {
            case 0:
                return "The " + getParameterName(0) + " determines the URL the endpoint, from which the entity profiles will be retrieved.";
            case 1:
                return "The " + getParameterName(1) + " determines the username that corresponds to an endpoint account with the necessary access rights.";
            case 3:
                return "The " + getParameterName(2) + " determines the password that corresponds to the username with the necessary access rights.";
            case 4:
                return "The " + getParameterName(3) + " determines the predicates that will be ignored during the creation of entity profiles.";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "SPARQL Endpoint URL";
            case 1:
                return "Username";
            case 2:
                return "Password";
            case 3:
                return "Predicates To Exclude";
            default:
                return "invalid parameter id";
        }
    }

    private void readEndpoint(String endpointUrl) throws IOException {
        //read each ntriples
        //get spo, create a separate profile for each separate subject,
        //with Attribute=predicate and Value=object
        String sparqlQueryString1 = "select ?a ?b ?c where {?a ?b ?c}";

        Query query = QueryFactory.create(sparqlQueryString1);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointUrl, query);
        ResultSet results = qexec.execSelect();
        //ResultSetFormatter.out(System.out, results, query);       
        //results = qexec.execSelect();

        while (results.hasNext()) {
            QuerySolution qs = results.next();

            String sub = qs.get("a").toString();
            String pred = qs.get("b").toString();
            String obj = qs.get("c").toString();
            if (attributesToExclude.contains(pred)) {
                continue;
            }

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
        qexec.close();
    }

    public void setAttributesToExclude(String[] attributesNamesToExclude) {
        attributesToExclude.addAll(Arrays.asList(attributesNamesToExclude));
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
