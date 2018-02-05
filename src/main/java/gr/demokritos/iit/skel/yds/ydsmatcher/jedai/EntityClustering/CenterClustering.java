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
import DataModel.SimilarityEdge;
import DataModel.SimilarityPairs;
import Utilities.Comparators.SimilarityEdgeComparator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class CenterClustering extends AbstractEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(CenterClustering.class.getName());

    public CenterClustering() {
        this(0.5);
    }
    
    public CenterClustering(double simTh) {
        super(simTh);

        LOGGER.log(Level.INFO, "{0} initiated", getMethodName());
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        initializeData(simPairs);
        initializeGraph();
        
        final double[] edgesWeight = new double[noOfEntities];
        final double[] edgesAttached = new double[noOfEntities];
        final Queue<SimilarityEdge> SEqueue = new PriorityQueue<>(simPairs.getNoOfComparisons(), new SimilarityEdgeComparator());

        final Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) { // add a similarity edge to the queue, not the for every pair of entities with a weight higher than the threshold
            Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
                SEqueue.add(new SimilarityEdge(comparison.getEntityId1(), comparison.getEntityId2() + datasetLimit, comparison.getUtilityMeasure()));

                edgesWeight[comparison.getEntityId1()] += comparison.getUtilityMeasure();
                edgesWeight[comparison.getEntityId2() + datasetLimit] += comparison.getUtilityMeasure();

                edgesAttached[comparison.getEntityId1()]++;
                edgesAttached[comparison.getEntityId2() + datasetLimit]++;
            }
        }

        final Set<Integer> Center = new HashSet<>();
        final Set<Integer> NonCenter = new HashSet<>();
        while (!SEqueue.isEmpty()) {
            SimilarityEdge se = SEqueue.remove();
            int v1 = se.getModel1Pos();
            int v2 = se.getModel2Pos();

            boolean v1IsCenter = Center.contains(v1);
            boolean v2IsCenter = Center.contains(v2);
            boolean v1IsNonCenter = NonCenter.contains(v1);
            boolean v2IsNonCenter = NonCenter.contains(v2);

            if (!(v1IsCenter || v2IsCenter || v1IsNonCenter || v2IsNonCenter)) {
                double w1 = edgesWeight[v1] / edgesAttached[v1];
                double w2 = edgesWeight[v2] / edgesAttached[v2];
                if (w1 > w2) {
                    Center.add(v1);
                    NonCenter.add(v2);
                } else {
                    Center.add(v2);
                    NonCenter.add(v1);
                }

                similarityGraph.addEdge(v1, v2);
            } else if ((v1IsCenter && v2IsCenter) || (v1IsNonCenter && v2IsNonCenter)) {
                continue;
            } else if (v1IsCenter && !v2IsNonCenter) {
                NonCenter.add(v2);
                similarityGraph.addEdge(v1, v2);
            } else if (v2IsCenter && !v1IsNonCenter) {
                NonCenter.add(v1);
                similarityGraph.addEdge(v1, v2);
            }
        }

        return getConnectedComponents();
    }

    @Override
    public String getMethodInfo() {
        return getMethodName() + ": it implements the CENTER algorithm such that a vertex is set as a cluster center if it has highest average edge weight from its adjacent nodes.";
    }
    
    @Override
    public String getMethodName() {
        return "Center Clustering";
    }
}
