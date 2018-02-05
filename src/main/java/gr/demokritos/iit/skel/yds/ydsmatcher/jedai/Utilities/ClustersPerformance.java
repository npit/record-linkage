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
package Utilities;

import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import DataModel.Comparison;
import DataModel.EquivalenceCluster;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */
public class ClustersPerformance {

    private static final Logger LOGGER = Logger.getLogger(ClustersPerformance.class.getName());

    private double fMeasure;
    private double precision;
    private double recall;
    private double totalMatches;

    private final AbstractDuplicatePropagation abstractDP;
    private final List<EquivalenceCluster> entityClusters;

    public ClustersPerformance(List<EquivalenceCluster> clusters, AbstractDuplicatePropagation adp) {
        abstractDP = adp;
        abstractDP.resetDuplicates();
        entityClusters = clusters;
    }

    public int getDetectedDuplicates() {
        return abstractDP.getNoOfDuplicates();
    }
    
    public int getEntityClusters() {
        return entityClusters.size();
    }

    public int getExistingDuplicates() {
        return abstractDP.getExistingDuplicates();
    }

    public double getFMeasure() {
        return fMeasure;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getTotalMatches() {
        return totalMatches;
    }

    public void printStatistics(double overheadTime, String methodName, String methodConfiguration) {
        System.out.println("\n\n\n**************************************************");
        System.out.println("Performance of : " + methodName);
        System.out.println("Configuration : " + methodConfiguration);
        System.out.println("**************************************************");
        System.out.println("No of clusters\t:\t" + entityClusters.size());
        System.out.println("Detected duplicates\t:\t" + abstractDP.getNoOfDuplicates());
        System.out.println("Existing duplicates\t:\t" + abstractDP.getExistingDuplicates());
        System.out.println("Total matches\t:\t" + totalMatches);
        System.out.println("Precision\t:\t" + precision);
        System.out.println("Recall\t:\t" + recall);
        System.out.println("F-Measure\t:\t" + fMeasure);
        System.out.println("Overhead time\t:\t" + overheadTime);
    }

    public void setStatistics() {
        if (entityClusters.isEmpty()) {
            LOGGER.log(Level.WARNING, "Empty set of equivalence clusters given as input!");
            return;
        }

        totalMatches = 0;
        if (abstractDP instanceof BilateralDuplicatePropagation) { // Clean-Clean ER
            for (EquivalenceCluster cluster : entityClusters) {
                for (int entityId1 : cluster.getEntityIdsD1()) {
                    for (int entityid2 : cluster.getEntityIdsD2()) {
                        totalMatches++;
                        Comparison comparison = new Comparison(true, entityId1, entityid2);
                        abstractDP.isSuperfluous(comparison);
                    }
                }
            }
        } else { // Dirty ER
            for (EquivalenceCluster cluster : entityClusters) {
                List<Integer> duplicates = cluster.getEntityIdsD1();
                Integer[] duplicatesArray = duplicates.toArray(new Integer[duplicates.size()]);

                for (int i = 0; i < duplicatesArray.length; i++) {
                    for (int j = i + 1; j < duplicatesArray.length; j++) {
                        totalMatches++;
                        Comparison comparison = new Comparison(false, duplicatesArray[i], duplicatesArray[j]);
                        abstractDP.isSuperfluous(comparison);
                    }
                }
            }
        }

        if (0 < totalMatches) {
            precision = abstractDP.getNoOfDuplicates() / totalMatches;
        } else {
            precision = 0;
        }
        recall = ((double) abstractDP.getNoOfDuplicates()) / abstractDP.getExistingDuplicates();
        if (0 < precision && 0 < recall) {
            fMeasure = 2 * precision * recall / (precision + recall);
        } else {
            fMeasure = 0;
        }
    }
}
