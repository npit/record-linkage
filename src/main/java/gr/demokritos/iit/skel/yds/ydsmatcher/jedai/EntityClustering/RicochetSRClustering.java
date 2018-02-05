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
package EntityClustering;

import DataModel.Comparison;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataModel.VertexWeight;
import Utilities.Comparators.VertexWeightComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Manos Thanos
 */
public class RicochetSRClustering extends AbstractEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(RicochetSRClustering.class.getName());

    public RicochetSRClustering() {
        this(0.5);
    }

    public RicochetSRClustering(double simTh) {
        super(simTh);

        LOGGER.log(Level.INFO, "{0} initiated", getMethodName());
    }
    
    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        initializeData(simPairs);

        final Queue<VertexWeight> VWqueue = new PriorityQueue<>(noOfEntities, new VertexWeightComparator());
        final double[] edgesWeight = new double[noOfEntities];
        final int[] edgesAttached = new int[noOfEntities];
        final List<Map<Integer, Double>> connections = new ArrayList<>();
        for (int i = 0; i < noOfEntities; i++) {
            connections.add(i, new HashMap<>());
        }

        final Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {	// add an edge for every pair of entities with a weight higher than the threshold
            Comparison comparison = iterator.next();
            int entityId2 = comparison.getEntityId2() + datasetLimit;
            if (threshold < comparison.getUtilityMeasure()) {
                edgesWeight[comparison.getEntityId1()] += comparison.getUtilityMeasure();
                edgesWeight[entityId2] += comparison.getUtilityMeasure();

                edgesAttached[comparison.getEntityId1()]++;
                edgesAttached[entityId2]++;

                connections.get(comparison.getEntityId1()).put(entityId2, comparison.getUtilityMeasure());
                connections.get(entityId2).put(comparison.getEntityId1(), comparison.getUtilityMeasure());
            }
        }

        for (int i = 0; i < noOfEntities; i++) {
            if (0 < edgesAttached[i]) {
                VWqueue.add(new VertexWeight(i, edgesWeight[i], edgesAttached[i], connections.get(i)));
            }
        }

        if (VWqueue.isEmpty()) {
            return new ArrayList<>();
        }

        final Set<Integer> Center = new HashSet<>();
        final Set<Integer> NonCenter = new HashSet<>();
        final Map<Integer, Set<Integer>> Clusters = new HashMap<>();
        final int[] clusterCenter = new int[noOfEntities];
        final double[] simWithCenter = new double[noOfEntities]; // similarity with center

        //Deal with the heaviest vertex first
        VertexWeight vw = VWqueue.remove();
        int v1 = vw.getPos();
        Center.add(v1);
        clusterCenter[v1] = v1;
        Clusters.put(v1, new HashSet<>());
        Clusters.get(v1).add(v1);//initialize v1 Cluster with its own value
        simWithCenter[v1] = 1.0;
        Map<Integer, Double> connect = vw.Connections();
        for (int v2 : connect.keySet()) {
            NonCenter.add(v2);
            clusterCenter[v2] = v1;
            simWithCenter[v2] = connect.get(v2);//similarity between v1 and v2
            Clusters.get(v1).add(v2);
        }

        while (!VWqueue.isEmpty()) {
            vw = VWqueue.remove();
            v1 = vw.getPos();
            connect = vw.Connections();
            final Set<Integer> toReassign = new HashSet<>();
            final Set<Integer> centersToReassign = new HashSet<>();
            for (int v2 : connect.keySet()) {
                if (Center.contains(v2)) {
                    continue;
                }
                double sim = connect.get(v2);
                double previousSim = simWithCenter[v2];
                if ((sim <= previousSim)) {
                    continue;
                }

                //Since we reach this point, v2 has to be put in v1's cluster
                toReassign.add(v2);
            }

            if (!toReassign.isEmpty()) {
                if (NonCenter.contains(v1)) { //if v1 was in another cluster already then deal with that cluster
                    NonCenter.remove(v1);
                    int prevClusterCenter = clusterCenter[v1];
                    Clusters.get(prevClusterCenter).remove(v1);
                    if (Clusters.get(prevClusterCenter).size() < 2) {//if v2's previous cluster becomes a singleton 
                        centersToReassign.add(prevClusterCenter);    //delete this cluster and put 
                    }
                }
                toReassign.add(v1);

                Clusters.put(v1, toReassign);
                Center.add(v1);
            }

            for (int v2 : toReassign) {
                if (v2 == v1) {
                    continue;
                }

                if (NonCenter.contains(v2)) {//if v2 was in another cluster already then deal with that cluster
                    int prevClusterCenter = clusterCenter[v2];
                    Clusters.get(prevClusterCenter).remove(v2);

                    if (Clusters.get(prevClusterCenter).size() < 2) {//if v2's previous cluster becomes a singleton 
                        centersToReassign.add(prevClusterCenter);
                    }
                }

                NonCenter.add(v2);
                clusterCenter[v2] = v1;
                simWithCenter[v2] = connect.get(v2);
            }

            for (int ctr : centersToReassign) {
                if (Clusters.get(ctr).size() > 1) {
                    continue;
                }
                Center.remove(ctr);
                Clusters.remove(ctr);

                double max = 0.0;
                int newCenter = v1;//in case there is no close similarity
                for (int center : Center) {
                    final Map<Integer, Double> currentConnections = connections.get(center);
                    Double newSim = currentConnections.get(ctr);
                    if (newSim != null) {
                        if (newSim > max) {
                            max = newSim;
                            newCenter = center;
                        }
                    }
                }

                Clusters.get(newCenter).add(ctr);
                NonCenter.add(ctr);
                clusterCenter[ctr] = newCenter;
                simWithCenter[ctr] = max;
            }
        }

        for (int i = 0; i < noOfEntities; i++) {
            if ((!NonCenter.contains(i)) && (!Center.contains(i))) {
                Center.add(i);
                clusterCenter[i] = i;
                Clusters.put(i, new HashSet<>(i));//initialize v1 Cluster with its own value
                simWithCenter[i] = 1.0;
            }
        }

        // get connected components
        final List<EquivalenceCluster> equivalenceClusters = new ArrayList<>();
        for (Set<Integer> componentIds : Clusters.values()) {
            EquivalenceCluster newCluster = new EquivalenceCluster();
            equivalenceClusters.add(newCluster);
            if (!simPairs.isCleanCleanER()) {
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

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it implements the Richochet Sequential Rippling algorithm.";
    }

    @Override
    public String getMethodName() {
        return "Ricochet Sequential Rippling Clustering";
    }
}
