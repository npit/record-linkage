package gr.demokritos.iit.skel.yds.ydsmatcher;

import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.*;

public class PropertiesParser {
    Properties props;
    ArrayList<String> opts;
    boolean verbosity;
    public void verbose(String msg){
        if(verbosity) System.out.println(msg);
    }
    public PropertiesParser(String propsFile){
        props = new Properties();
        // read properties file
        try {
            props.load(new FileReader(propsFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        verbosity = props.getProperty("verbosity","").trim().toLowerCase().equals("true");

    }
    // methods retrieving props jedai parameters
    public SimilarityMetric getSimilarity(){
        String similarity_str = props.getProperty("similarity").trim();

        if(similarity_str.toLowerCase().equals("jaccard"))
            return SimilarityMetric.JACCARD_SIMILARITY;
        else if(similarity_str.toLowerCase().equals("cosine"))
            return SimilarityMetric.COSINE_SIMILARITY;
        else if(similarity_str.toLowerCase().equals("nvs"))
            return SimilarityMetric.GRAPH_NORMALIZED_VALUE_SIMILARITY;
        else{
            System.err.println("Undefined similarity param: [" + similarity_str + "]");
            return null;
        }
    }
    public String getSimilarityCSVField(){ return props.getProperty("sim_field","").trim();}
    public String getReadMode(){
        return props.getProperty("read_mode","").trim();
    }
    public boolean getVerbosity(){
        return verbosity;
    }
    public List<String> getLanguages(){
        String langs = props.getProperty("languages","").trim();
        if (langs.isEmpty()) return new ArrayList<>();
        String [] parts = langs.trim().split(",");
        for(int i=0;i<parts.length;++i){
            parts[i] = parts[i].trim();
            verbose("Read lang: [" + parts[i]+"]");
        }
        return Arrays.asList(parts);
    }
    public double getClusteringThreshold(){
        return Double.parseDouble(props.getProperty("clustering_threshold").trim());
    }
    public RepresentationModel getRepresentation(){
        String repr_str = props.getProperty("representation").trim();

        if(repr_str.toLowerCase().startsWith("bow_tfidf")) {
            if(repr_str.endsWith("_w1")) return RepresentationModel.TOKEN_UNIGRAMS_TF_IDF;
            if(repr_str.endsWith("_w2")) return RepresentationModel.TOKEN_BIGRAMS_TF_IDF;
            if(repr_str.endsWith("_w3")) return RepresentationModel.TOKEN_TRIGRAMS_TF_IDF;
        }
        else if(repr_str.toLowerCase().startsWith("bow")) {
            if(repr_str.endsWith("_w1")) return RepresentationModel.TOKEN_UNIGRAMS;
            if(repr_str.endsWith("_w2")) return RepresentationModel.TOKEN_BIGRAMS;
            if(repr_str.endsWith("_w3")) return RepresentationModel.TOKEN_TRIGRAMS;
        }
        else if(repr_str.toLowerCase().startsWith("ngg")) {
            if(repr_str.endsWith("_w1")) return RepresentationModel.TOKEN_UNIGRAM_GRAPHS;
            if(repr_str.endsWith("_w2")) return RepresentationModel.TOKEN_BIGRAM_GRAPHS;
            if(repr_str.endsWith("_w3")) return RepresentationModel.TOKEN_TRIGRAM_GRAPHS;
            if(repr_str.endsWith("_c2")) return RepresentationModel.CHARACTER_BIGRAM_GRAPHS;
            if(repr_str.endsWith("_c3")) return RepresentationModel.CHARACTER_TRIGRAM_GRAPHS;
            if(repr_str.endsWith("_c4")) return RepresentationModel.CHARACTER_FOURGRAM_GRAPHS;
        }
        System.err.println("Undefined representation param: [" + repr_str + "]");
        return null;

    }
    public String getInputPath(){
        return props.getProperty("input_path","").trim();
    }
    public String getReadOrder(){
        return props.getProperty("read_order","").trim();
    }
    public boolean hasOption(String opt){
        if (opts == null){
            this.opts = new ArrayList<>();
            String o = props.getProperty("options","");
            String [] parts = o.split(",");
            for(String s: parts) this.opts.add(s.trim());
        }
        return opts.contains(opt);
    }

}
