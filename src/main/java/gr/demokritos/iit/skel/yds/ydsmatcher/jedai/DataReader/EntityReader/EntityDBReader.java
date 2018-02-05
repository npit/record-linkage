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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

/**
 *
 * @author G.A.P. II
 */
public class EntityDBReader extends AbstractEntityReader {

    private static final Logger LOGGER = Logger.getLogger(EntityDBReader.class.getName());

    private boolean ssl;

    private String password;
    private String table;
    private String user;

    private final Set<String> attributesToExclude;

    public EntityDBReader(String dbURL) {
        super(dbURL);
        
        password = null;
        ssl = true;
        table = null;
        user = null;
        
        attributesToExclude = new HashSet<>();
    }

    @Override
    public List<EntityProfile> getEntityProfiles() {
        if (!entityProfiles.isEmpty()) {
            return entityProfiles;
        }

        if (inputFilePath == null) {
            LOGGER.log(Level.SEVERE, "Database url has not been set!");
            return null;
        }

        //inputFilePath is assigned the Database URL
        try {
            if (user == null) {
                LOGGER.log(Level.SEVERE, "Database user has not been set!");
                return null;
            }
            if (password == null) {
                LOGGER.log(Level.SEVERE, "Database password has not been set!");
                return null;
            }
            if (table == null) {
                LOGGER.log(Level.SEVERE, "Database table has not been set!");
                return null;
            }

            Connection conn = null;
            if (inputFilePath.startsWith("mysql")) {
                conn = getMySQLconnection(inputFilePath);
            } else if (inputFilePath.startsWith("postgresql")) {
                conn = getPostgreSQLconnection(inputFilePath);
            } else {
                LOGGER.log(Level.SEVERE, "Only MySQL and PostgreSQL are supported for the time being!");
                return null;
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);//retrieve the appropriate table
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNum = rsmd.getColumnCount();
            String[] columns = new String[columnsNum];
            for (int i = 0; i < columnsNum; i++) {
                columns[i] = rsmd.getColumnName(i + 1);//get attribute names
            }

            //Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String id = rs.getString(columns[0]);
                EntityProfile newProfile = new EntityProfile(id);//create a new profile for each record
                entityProfiles.add(newProfile);
                for (int i = 1; i < columnsNum; i++) {
                    String attributeName = columns[i];
                    if (attributesToExclude.contains(attributeName)) {
                        continue;
                    }

                    String value = rs.getString(columns[i]);
                    newProfile.addAttribute(attributeName, value);
                }
            }
            rs.close();
        } catch (Exception ex) {
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
                + getParameterName(1) + "=" + table + "\t"
                + getParameterName(2) + "=" + user + "\t"
                + getParameterName(3) + "=" + password + "\t"
                + getParameterName(4) + "=" + sb.toString() + "\t"
                + getParameterName(5) + "=" + ssl;
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it converts a relational database (MySQL or PostgreSQL) into a set of entity profiles.";
    }

    @Override
    public String getMethodName() {
        return "Database Reader";
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " involves six parameters:\n"
                + "1)" + getParameterDescription(0) + ".\n"
                + "2)" + getParameterDescription(1) + ".\n"
                + "3)" + getParameterDescription(2) + ".\n"
                + "4)" + getParameterDescription(3) + ".\n"
                + "5)" + getParameterDescription(4) + ".\n"
                + "6)" + getParameterDescription(5) + ".";
    }

    private Connection getMySQLconnection(String dbURL) throws IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:" + dbURL + "?user=" + user + "&password=" + password);
            return conn;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
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
        obj4.put("class", "java.lang.String");
        obj4.put("name", getParameterName(3));
        obj4.put("defaultValue", "-");
        obj4.put("minValue", "-");
        obj4.put("maxValue", "-");
        obj4.put("stepValue", "-");
        obj4.put("description", getParameterDescription(3));
        
        JsonObject obj5 = new JsonObject();
        obj5.put("class", "java.util.Set<String>");
        obj5.put("name", getParameterName(4));
        obj5.put("defaultValue", "-");
        obj5.put("minValue", "-");
        obj5.put("maxValue", "-");
        obj5.put("stepValue", "-");
        obj5.put("description", getParameterDescription(4));
        
        JsonObject obj6 = new JsonObject();
        obj6.put("class", "java.lang.Boolean");
        obj6.put("name", getParameterName(5));
        obj6.put("defaultValue", "true");
        obj6.put("minValue", "-");
        obj6.put("maxValue", "-");
        obj6.put("stepValue", "-");
        obj6.put("description", getParameterDescription(5));

        JsonArray array = new JsonArray();
        array.add(obj1);
        array.add(obj2);
        array.add(obj3);
        array.add(obj4);
        array.add(obj5);
        array.add(obj6);
        return array;
    }

    @Override
    public String getParameterDescription(int parameterId) {
        switch (parameterId) {
            case 0:
                return "The " + getParameterName(0) + " determines the URL and the port of the relational database, from which the entity profiles will be retrieved.";
            case 1:
                return "The " + getParameterName(1) + " determines the name of the database table that contains the entity profiles.";
            case 2:
                return "The " + getParameterName(2) + " determines the username that corresponds to a database account with access rights to the specified database and table.";
            case 3:
                return "The " + getParameterName(3) + " determines the password that corresponds to the username with access rights to the specified database and table.";
            case 4:
                return "The " + getParameterName(4) + " determines the names of the columns that will be ignored during the creation of entity profiles.";
            case 5:
                return "The " + getParameterName(5) + " determines whether the database connection will use SSL to encrypt client/server communications for increased security.";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "URL";
            case 1:
                return "Table";
            case 2:
                return "Username";
            case 3:
                return "Password";
            case 4:
                return "Attributes To Exclude";
            case 5:
                return "SSL";
            default:
                return "invalid parameter id";
        }
    }

    private Connection getPostgreSQLconnection(String dbURL) throws IOException {
        try {
            Properties props = new Properties();
            if (!(user == null)) {
                props.setProperty("user", user);
            }
            if (!(password == null)) {
                props.setProperty("password", password);
            }
            if (ssl) {
                props.setProperty("ssl", "true");
            }
            return DriverManager.getConnection("jdbc:" + dbURL, props);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void setAttributesToExclude(String[] attributesNamesToExclude) {
        attributesToExclude.addAll(Arrays.asList(attributesNamesToExclude));
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setSSL(boolean ssl) {
        this.ssl = ssl;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
