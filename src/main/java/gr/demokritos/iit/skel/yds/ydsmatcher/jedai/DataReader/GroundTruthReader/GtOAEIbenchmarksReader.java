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
package DataReader.GroundTruthReader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author G.A.P. II
 */
public class GtOAEIbenchmarksReader extends GtRDFReader {

    private static final Logger LOGGER = Logger.getLogger(GtOAEIbenchmarksReader.class.getName());

    public GtOAEIbenchmarksReader(String filePath) {
        super(filePath);
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it converts an XML ground-truth file of an OAEI Benchmark dataset into a set of pairs of duplicate entity profiles.";
    }

    @Override
    public String getMethodName() {
        return "RDF OAEI Benchmark Ground-truth Reader";
    }
    
    // we keep as duplicates the entity1 and entity2 instances
    // of every "Cell" in the goldenStandard xml file
    @Override
    protected void performReading() {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(inputFilePath);
            doc.getDocumentElement().normalize();
            
            final NodeList nList = doc.getElementsByTagName("Cell");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    Element eElement1 = (Element) eElement.getElementsByTagName("entity1").item(0);
                    int entityId1 = urlToEntityId1.get(eElement1.getAttribute("rdf:resource"));
                    
                    Element eElement2 = (Element) eElement.getElementsByTagName("entity2").item(0);
                    int entityId2 = urlToEntityId2.get(eElement2.getAttribute("rdf:resource"));
                    
                    duplicatesGraph.addEdge(entityId1, entityId2);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
