/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.demokritos.iit.skel.yds.ydsmatcher;

import BlockBuilding.IBlockBuilding;
import BlockBuilding.StandardBlocking;
import BlockProcessing.BlockRefinement.SizeBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.WeightedEdgePruning;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.Attribute;
import DataModel.EntityProfile;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.EntityReader.EntityCSVReader;
import EntityClustering.IEntityClustering;
import EntityClustering.RicochetSRClustering;
import EntityMatching.ProfileMatcher;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;

import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author ggianna
 */
public class YDSMatcher {

    public static void main(String[] args) {

        // Maximum list length parameter
        int iMaxListSize = Integer.MAX_VALUE;
        if (args.length < 1){
            System.err.println("Missing parameters file");
            return;
        }
        // read all relevant properties
        PropertiesParser pparser = new PropertiesParser(args[0]);
        String input_file = pparser.getInput();
        if (input_file.isEmpty()){
            System.err.println("Missing input parameter!");
            return;
        }
        // read entities from text files
        SimpleReader sr = new SimpleReader();
        List<EntityProfile> lpEntities = sr.read_data(input_file);

        // Read entities
        // EntityCSVReader ecrReader = new EntityCSVReader(input_file);
        // ecrReader.setAttributeNamesInFirstRow(true);
        // List<EntityProfile> lpEntities = ecrReader.getEntityProfiles().subList(0, iMaxListSize);

        // TODO: Cache results
        boolean bCacheOK = false;
        SimilarityPairs lspPairs = null;
        SimilarityMetric sim = pparser.getSimilarity();
        RepresentationModel repr = pparser.getRepresentation();
        if (!bCacheOK) {
            // Create and process blocks
            IBlockBuilding block = new StandardBlocking();
            List<AbstractBlock> lbBlocks = block.getBlocks(lpEntities);

            IBlockProcessing bpProcessor = new SizeBasedBlockPurging();
            lbBlocks = bpProcessor.refineBlocks(lbBlocks);

            IBlockProcessing bpComparisonCleaning = new WeightedEdgePruning();
            lbBlocks = bpComparisonCleaning.refineBlocks(lbBlocks);

            // Measure similarities
            ProfileMatcher pm = new ProfileMatcher(repr, sim);
            lspPairs = pm.executeComparisons(lbBlocks, lpEntities);
        }

        // Perform clustering
        IEntityClustering ie = new RicochetSRClustering();
        List<EquivalenceCluster> lClusters = ie.getDuplicates(lspPairs);

        // Show clusters
        // For every cluster
        for (EquivalenceCluster ecCur : lClusters) {
            System.out.println("--- Cluster " + ecCur.toString() + " :");
            List<Integer> liFirst = ecCur.getEntityIdsD1();

            // Second list not applicable in "dirty list" scenario
            // Using only first list
            ListIterator<Integer> li1 = liFirst.listIterator();

            // For each entity in cluster
            while (li1.hasNext()) {
                // get index
                int i1 = li1.next();

                // get entities
                EntityProfile ep1 = lpEntities.get(i1);

                // Output profiles
                System.out.println(entityProfileToString(ep1));
            }
        }

    }

    public static String entityProfileToString(EntityProfile epToRender) {
        StringBuffer sb = new StringBuffer();

        for (Attribute aCur : epToRender.getAttributes()) {
            sb.append("\t").append(aCur.getName()).append("=");
            sb.append(aCur.getValue()).append("\n");
        }

        return sb.toString();
    }
}
