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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
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
            if (repr == null) return;
            if (!bCacheOK) {
                Timer.tic("block building");
                // Create and process blocks
                IBlockBuilding block = new StandardBlocking();
                List<AbstractBlock> lbBlocks = block.getBlocks(lpEntities);

                IBlockProcessing bpProcessor = new SizeBasedBlockPurging();
                lbBlocks = bpProcessor.refineBlocks(lbBlocks);

                IBlockProcessing bpComparisonCleaning = new WeightedEdgePruning();
                lbBlocks = bpComparisonCleaning.refineBlocks(lbBlocks);
                Timer.tell("block building");

                // Measure similarities
                Timer.tic("Similarity mapping");
                ProfileMatcher pm = new ProfileMatcher(repr, sim);
                lspPairs = pm.executeComparisons(lbBlocks, lpEntities);
                Timer.tell("Similarity mapping");
                verbose("Extracted " + lspPairs.getSimilarities().length + " similarity pairs.");
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

        if (pparser.hasOption("write_sim")) writeSimilarities(lspPairs, sr.getReadOrder(pparser.getReadOrder()));

        // Perform clustering
        Timer.tic("Clustering");
        verbose("\nClustering");
        double clustering_threshold = pparser.getClusteringThreshold();
        IEntityClustering ie = new RicochetSRClustering(clustering_threshold);
        List<EquivalenceCluster> lClusters = ie.getDuplicates((SimilarityPairs)lspPairs);
        Timer.tell("Clustering");
        verbose("Done clustering");

        if(lClusters.isEmpty()) verbose("No clusters!");

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

    public static void writeSimilarities(SimilarityPairs sp, List<Pair<String,Integer>> rdo){
        verbose("Writing similarities.");
        HashMap<Integer,String> rdomap = new HashMap<>();
        for(Pair<String,Integer> p : rdo) {
            if (rdomap.containsKey(p.d2)){ System.out.println("Already in map:" + p.d2); return;}
            rdomap.put(p.d2, p.d1);
        }
        try{
            BufferedWriter bf = new BufferedWriter(new FileWriter("similarities.txt"));
            bf.write("name1,name2,sim\n");
            int count=0;
            for(int i=0;i<sp.getEntityIds1().length;++i){
                int i1 = sp.getEntityIds1()[i];
                int i2 = sp.getEntityIds2()[i];
                if(!rdomap.containsKey(i1)) {
                    System.out.println("Not in map:" + i1); return;
                }
                if(!rdomap.containsKey(i2)) {
                    System.out.println("Not in map:" + i2); return;
                }
                String name1 = rdomap.get(i1);
                String name2 = rdomap.get(i2);
                double sim = sp.getSimilarities()[i];
                // System.out.print(name1 + "," + name2 + "," + sim + "\n");
                bf.write(name1 + "," + name2 + "," + sim + "\n");
                ++count;
            }
            bf.close();
            verbose("Wrote " + count + "similarities.");
        }
            catch(Exception ex){
                ex.printStackTrace();
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
