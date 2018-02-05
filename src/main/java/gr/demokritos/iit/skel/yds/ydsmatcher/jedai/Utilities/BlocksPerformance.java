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

import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.GroundTruthIndex;
import DataModel.AbstractBlock;
import DataModel.BilateralBlock;
import DataModel.Comparison;
import DataModel.ComparisonIterator;
import DataModel.DecomposedBlock;
import DataModel.IdDuplicates;
import DataModel.UnilateralBlock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */
public class BlocksPerformance {

    private static final Logger LOGGER = Logger.getLogger(BlocksPerformance.class.getName());

    private boolean isCleanCleanER;

    private int noOfD1Entities;
    private int noOfD2Entities;
    private int detectedDuplicates;

    private double aggregateCardinality;
    private double blockAssignments;
    private double d1BlockAssignments;
    private double d2BlockAssignments;
    private double fMeasure;
    private double pc;
    private double pq;

    private final AbstractDuplicatePropagation abstractDP;
    private final List<AbstractBlock> blocks;
    private GroundTruthIndex entityIndex;

    public BlocksPerformance(List<AbstractBlock> bl, AbstractDuplicatePropagation adp) {
        abstractDP = adp;
        abstractDP.resetDuplicates();
        blocks = bl;
    }

    private boolean areCooccurring(boolean cleanCleanER, IdDuplicates pairOfDuplicates) {
        int[] blocks1 = entityIndex.getEntityBlocks(pairOfDuplicates.getEntityId1(), 0);
        if (blocks1 == null) {
            return false;
        }

        int[] blocks2 = entityIndex.getEntityBlocks(pairOfDuplicates.getEntityId2(), cleanCleanER ? 1 : 0);
        if (blocks2 == null) {
            return false;
        }

        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        for (int i = 0; i < noOfBlocks1; i++) {
            for (int j = 0; j < noOfBlocks2; j++) {
                if (blocks2[j] < blocks1[i]) {
                    continue;
                }

                if (blocks1[i] < blocks2[j]) {
                    break;
                }

                if (blocks1[i] == blocks2[j]) {
                    return true;
                }
            }
        }

        return false;
    }

    public double getAggregateCardinality() {
        return aggregateCardinality;
    }

    public double getBlockAssignments() {
        return blockAssignments;
    }

    public double getD1BlockAssignments() {
        return d1BlockAssignments;
    }

    public double getD2BlockAssignments() {
        return d2BlockAssignments;
    }

    public double getPc() {
        return pc;
    }

    public double getPq() {
        return pq;
    }

    private void getBilateralBlockingCardinality() {
        d1BlockAssignments = 0;
        d2BlockAssignments = 0;
        for (AbstractBlock block : blocks) {
            BilateralBlock bilBlock = (BilateralBlock) block;
            d1BlockAssignments += bilBlock.getIndex1Entities().length;
            d2BlockAssignments += bilBlock.getIndex2Entities().length;
        }
    }

    private void getDecomposedBlocksEntities() {
        final Set<Integer> entitiesD1 = new HashSet<Integer>((int) aggregateCardinality);
        if (isCleanCleanER) {
            final Set<Integer> entitiesD2 = new HashSet<Integer>((int) aggregateCardinality);
            for (AbstractBlock block : blocks) {
                ComparisonIterator iterator = block.getComparisonIterator();
                while (iterator.hasNext()) {
                    Comparison comparison = iterator.next();
                    entitiesD1.add(comparison.getEntityId1());
                    entitiesD2.add(comparison.getEntityId2());
                }
            }
            noOfD1Entities = entitiesD1.size();
            noOfD2Entities = entitiesD2.size();
        } else {
            for (AbstractBlock block : blocks) {
                ComparisonIterator iterator = block.getComparisonIterator();
                while (iterator.hasNext()) {
                    Comparison comparison = iterator.next();
                    entitiesD1.add(comparison.getEntityId1());
                    entitiesD1.add(comparison.getEntityId2());
                }
            }
            noOfD1Entities = entitiesD1.size();
        }
    }

    public int getDetectedDuplicates() {
        return detectedDuplicates;
    }

    private void getDuplicatesOfDecomposedBlocks() {
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                abstractDP.isSuperfluous(iterator.next());
            }
        }

        detectedDuplicates = abstractDP.getNoOfDuplicates();
        pc = ((double) abstractDP.getNoOfDuplicates()) / abstractDP.getExistingDuplicates();
        pq = abstractDP.getNoOfDuplicates() / aggregateCardinality;
        fMeasure =  2 * pc * pq / (pc + pq);
    }

    private void getDuplicatesWithEntityIndex() {
        double noOfDuplicates = 0;
        boolean cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        for (IdDuplicates pairOfDuplicates : abstractDP.getDuplicates()) {
            if (areCooccurring(cleanCleanER, pairOfDuplicates)) {
                noOfDuplicates++;
            }
        }

        detectedDuplicates = (int) noOfDuplicates;
        pc = noOfDuplicates / abstractDP.getExistingDuplicates();
        pq = noOfDuplicates / aggregateCardinality;
        fMeasure =  2 * pc * pq / (pc + pq);
    }

    private void getEntities() {
        if (blocks.get(0) instanceof UnilateralBlock) {
            final Set<Integer> distinctEntities = new HashSet<Integer>();
            for (AbstractBlock block : blocks) {
                UnilateralBlock uBlock = (UnilateralBlock) block;
                for (int entityId : uBlock.getEntities()) {
                    distinctEntities.add(entityId);
                }
            }
            noOfD1Entities = distinctEntities.size();
        } else {
            final Set<Integer> distinctEntitiesD1 = new HashSet<Integer>();
            final Set<Integer> distinctEntitiesD2 = new HashSet<Integer>();
            for (AbstractBlock block : blocks) {
                BilateralBlock bBlock = (BilateralBlock) block;
                for (int entityId : bBlock.getIndex1Entities()) {
                    distinctEntitiesD1.add(entityId);
                }
                for (int entityId : bBlock.getIndex2Entities()) {
                    distinctEntitiesD2.add(entityId);
                }
            }
            noOfD1Entities = distinctEntitiesD1.size();
            noOfD2Entities = distinctEntitiesD2.size();
        }
    }

    public void printStatistics(double overheadTime, String methodConfiguration, String methodName) {
        if (blocks.isEmpty()) {
            return;
        }
        
        System.out.println("\n\n\n**************************************************");
        System.out.println("Performance of : " + methodName);
        System.out.println("Configuration : " + methodConfiguration);
        System.out.println("**************************************************");
        System.out.println("No of blocks\t:\t" + blocks.size());
        System.out.println("Aggregate cardinality\t:\t" + aggregateCardinality);
        System.out.println("CC\t:\t" + (blockAssignments / aggregateCardinality));
        if (blocks.get(0) instanceof BilateralBlock) {
            System.out.println("Total entities D1\t:\t" + entityIndex.getDatasetLimit());
            System.out.println("Singleton entities D1\t:\t" + (entityIndex.getDatasetLimit() - noOfD1Entities));
            System.out.println("Total entities D2\t:\t" + (entityIndex.getNoOfEntities() - entityIndex.getDatasetLimit()));
            System.out.println("Singleton entities D2\t:\t" + (entityIndex.getNoOfEntities() - entityIndex.getDatasetLimit() - noOfD2Entities));
            System.out.println("Entities in blocks\t:\t" + (noOfD1Entities + noOfD2Entities));
            System.out.println("Average block\t:\t" + d1BlockAssignments / blocks.size() + "-" + d2BlockAssignments / blocks.size());
            System.out.println("iBC_1\t:\t" + d1BlockAssignments / noOfD1Entities);
            System.out.println("iBC_2\t:\t" + d2BlockAssignments / noOfD2Entities);
            System.out.println("oBC\t:\t" + ((d1BlockAssignments + d2BlockAssignments) / (noOfD1Entities + noOfD2Entities)));
        } else if (blocks.get(0) instanceof DecomposedBlock) {
            if (isCleanCleanER) {
                System.out.println("Entities in blocks\t:\t" + (noOfD1Entities + noOfD2Entities));
            } else {
                System.out.println("Entities in blocks\t:\t" + noOfD1Entities);
            }
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            System.out.println("Total entities\t:\t" + entityIndex.getNoOfEntities());
            System.out.println("Entities in blocks\t:\t" + noOfD1Entities);
            System.out.println("Singleton entities\t:\t" + (entityIndex.getNoOfEntities() - noOfD1Entities));
            System.out.println("Average block\t:\t" + blockAssignments / blocks.size());
            System.out.println("BC\t:\t" + blockAssignments / noOfD1Entities);
        }
        System.out.println("Detected duplicates\t:\t" + detectedDuplicates);
        System.out.println("PC\t:\t" + pc);
        System.out.println("PQ\t:\t" + pq);
        System.out.println("F-Measure\t:\t" + fMeasure);
        System.out.println("Overhead time\t:\t" + overheadTime);
    }

    private void setComparisonsCardinality() {
        aggregateCardinality = 0;
        blockAssignments = 0;
        for (AbstractBlock block : blocks) {
            aggregateCardinality += block.getNoOfComparisons();
            blockAssignments += block.getTotalBlockAssignments();
        }
    }

    public void setStatistics() {
        if (blocks.isEmpty()) {
            LOGGER.log(Level.WARNING, "Empty set of equivalence clusters given as input!");
            return;
        }

        setType();
        setComparisonsCardinality();
        if (blocks.get(0) instanceof DecomposedBlock) {
            getDecomposedBlocksEntities();
        } else {
            entityIndex = new GroundTruthIndex(blocks, abstractDP.getDuplicates());
            getEntities();
        }
        if (blocks.get(0) instanceof BilateralBlock) {
            getBilateralBlockingCardinality();
        }
        if (blocks.get(0) instanceof DecomposedBlock) {
            getDuplicatesOfDecomposedBlocks();
        } else {
            getDuplicatesWithEntityIndex();
        }
    }

    private void setType() {
        if (blocks.get(0) instanceof BilateralBlock) {
            isCleanCleanER = true;
        } else if (blocks.get(0) instanceof DecomposedBlock) {
            DecomposedBlock deBlock = (DecomposedBlock) blocks.get(0);
            isCleanCleanER = deBlock.isCleanCleanER();
        } else if (blocks.get(0) instanceof UnilateralBlock) {
            isCleanCleanER = false;
        }
    }
}
