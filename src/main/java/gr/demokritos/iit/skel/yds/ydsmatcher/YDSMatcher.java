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
        Timer timer = new Timer();
        // Maximum list length parameter
        int iMaxListSize = Integer.MAX_VALUE;
        if (args.length < 1){
            System.err.println("Missing parameters file");
            return;
        }
        // read all relevant properties
        PropertiesParser pparser = new PropertiesParser(args[0]);
        String input_path = pparser.getInputPath();
        String read_order_file = pparser.getReadOrder();
        if (input_path.isEmpty()){
            System.err.println("Missing input parameter!");
            return;
        }
        YDSMatcher.verbosity = pparser.getVerbosity();
        SimilarityPairs lspPairs = null;
        List<EntityProfile> lpEntities = null;
        // read entities from text files
        String read_mode = pparser.getReadMode();
        SimpleReader sr = new SimpleReader(pparser.getLanguages(), pparser.getVerbosity(), read_mode);

        if (! read_mode.equals("similarities")) {
            verbose("\nReading data.");
            lpEntities = sr.read_data(input_path, read_order_file);
            verbose("Done reading data!");
            if (lpEntities.isEmpty()) {
                verbose("No data to cluster!");
                return;
            } else verbose("Working on " + lpEntities.size() + " data.");

            // Read entities
            // EntityCSVReader ecrReader = new EntityCSVReader(input_file);
            // ecrReader.setAttributeNamesInFirstRow(true);
            // List<EntityProfile> lpEntities = ecrReader.getEntityProfiles().subList(0, iMaxListSize);

            // TODO: Cache results
            verbose("\nMapping to representations");
            boolean bCacheOK = false;
            SimilarityMetric sim = pparser.getSimilarity();
            RepresentationModel repr = pparser.getRepresentation();
            if (!bCacheOK) {
                timer.tic("block building");
                // Create and process blocks
                IBlockBuilding block = new StandardBlocking();
                List<AbstractBlock> lbBlocks = block.getBlocks(lpEntities);

                IBlockProcessing bpProcessor = new SizeBasedBlockPurging();
                lbBlocks = bpProcessor.refineBlocks(lbBlocks);

                IBlockProcessing bpComparisonCleaning = new WeightedEdgePruning();
                lbBlocks = bpComparisonCleaning.refineBlocks(lbBlocks);
                timer.toc("block building");

                // Measure similarities
                timer.tic("Similarity mapping");
                ProfileMatcher pm = new ProfileMatcher(repr, sim);
                lspPairs = pm.executeComparisons(lbBlocks, lpEntities);
                timer.toc("Similarity mapping");
            }
            verbose("Done mapping comparisons");
        }
        else{
            // directly use supplied similarities from a file
            lspPairs = sr.readSimilaritiesFile(pparser.getInputPath(),pparser.getReadOrder(),pparser.getSimilarityCSVField());
            if (lspPairs == null){
                return;
            }

        }

        // Perform clustering
        timer.tic("Clustering");
        verbose("\nClustering");
        IEntityClustering ie = new RicochetSRClustering();
        List<EquivalenceCluster> lClusters = ie.getDuplicates((SimilarityPairs)lspPairs);
        timer.toc("Clustering");
        verbose("Done clustering");

        // Show clusters
        // For every cluster
        int totalCount = 0;
        int nonZeroCount = 0;
        for (EquivalenceCluster ecCur : lClusters) {
            ++ totalCount;
            List<Integer> liFirst = ecCur.getEntityIdsD1();
            if(liFirst.isEmpty()) continue;
            ++ nonZeroCount;
            verbose(String.format("Cluster %d/%d (%d non-zero)", totalCount, lClusters.size(), nonZeroCount) + " :");

            // Second list not applicable in "dirty list" scenario
            // Using only first list
            ListIterator<Integer> li1 = liFirst.listIterator();

            // For each entity in cluster
            while (li1.hasNext()) {
                // get index
                int i1 = li1.next();
                System.out.print(i1 + " ");
            }
            System.out.println();
        }

    }
    static boolean verbosity;

    public static void verbose(String msg){
        if(!YDSMatcher.verbosity) return;
        System.out.println(msg);
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
