package EntityClustering;

import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author G.A.P. II
 */
public abstract class AbstractEntityClustering implements IEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(AbstractEntityClustering.class.getName());

    protected boolean isCleanCleanER;

    protected double threshold;

    protected int noOfEntities;
    protected int datasetLimit;

    protected SimpleGraph similarityGraph;

    public AbstractEntityClustering(double simTh) {
        threshold = simTh;
    }

    protected List<EquivalenceCluster> getConnectedComponents() {
        // get connected components
        final ConnectivityInspector ci = new ConnectivityInspector(similarityGraph);
        final List<Set<Integer>> connectedComponents = ci.connectedSets();

        // prepare output
        final List<EquivalenceCluster> equivalenceClusters = new ArrayList<>();
        for (Set<Integer> componentIds : connectedComponents) {
            EquivalenceCluster newCluster = new EquivalenceCluster();
            equivalenceClusters.add(newCluster);

            if (!isCleanCleanER) {
                newCluster.loadBulkEntityIdsD1(componentIds);
                continue;
            }

            componentIds.forEach((entityId) -> {
                if (entityId < datasetLimit) {
                    newCluster.addEntityIdD1(entityId);
                } else {
                    newCluster.addEntityIdD2(entityId - datasetLimit);
                }
            });
        }

        return equivalenceClusters;
    }

    protected int getMaxEntityId(int[] entityIds) {
        int maxId = Integer.MIN_VALUE;
        for (int i = 0; i < entityIds.length; i++) {
            if (maxId < entityIds[i]) {
                maxId = entityIds[i];
            }
        }
        return maxId;
    }

    @Override
    public String getMethodConfiguration() {
        return getParameterName(0) + "=" + threshold;
    }

    @Override
    public String getMethodParameters() {
        return getMethodName() + " involves a single parameter:\n"
               + "1)" + getParameterDescription(0) + ".\n";
    }

    @Override
    public JsonArray getParameterConfiguration() {
        JsonObject obj1 = new JsonObject();
        obj1.put("class", "java.lang.Double");
        obj1.put("name", getParameterName(0));
        obj1.put("defaultValue", "0.5");
        obj1.put("minValue", "0.1");
        obj1.put("maxValue", "0.95");
        obj1.put("stepValue", "0.05");
        obj1.put("description", getParameterDescription(0));

        JsonArray array = new JsonArray();
        array.add(obj1);
        return array;
    }

    @Override
    public String getParameterDescription(int parameterId) {
        switch (parameterId) {
            case 0:
                return "The " + getParameterName(0) + " determines the cut-off similarity threshold for connecting two entities with an edge in the (initial) similarity graph.";
            default:
                return "invalid parameter id";
        }
    }

    @Override
    public String getParameterName(int parameterId) {
        switch (parameterId) {
            case 0:
                return "Similarity Threshold";
            default:
                return "invalid parameter id";
        }
    }

    protected void initializeData(SimilarityPairs simPairs) {
        isCleanCleanER = simPairs.isCleanCleanER();

        int maxEntity1 = getMaxEntityId(simPairs.getEntityIds1());
        int maxEntity2 = getMaxEntityId(simPairs.getEntityIds2());
        if (simPairs.isCleanCleanER()) {
            datasetLimit = maxEntity1 + 1;
            noOfEntities = maxEntity1 + maxEntity2 + 2;
        } else {
            datasetLimit = 0;
            noOfEntities = Math.max(maxEntity1, maxEntity2) + 1;
        }
    }

    protected void initializeGraph() {
        similarityGraph = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < noOfEntities; i++) {
            similarityGraph.addVertex(i);
        }
        LOGGER.log(Level.INFO, "Added {0} nodes in the graph", noOfEntities);
    }

    @Override
    public void setSimilarityThreshold(double th) {
        threshold = th;
        LOGGER.log(Level.INFO, "Similarity threshold : {0}", threshold);
    }
}
