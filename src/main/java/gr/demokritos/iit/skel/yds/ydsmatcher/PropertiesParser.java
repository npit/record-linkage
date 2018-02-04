package gr.demokritos.iit.skel.yds.ydsmatcher;

import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.*;

public class PropertiesParser {
    Properties props;
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
        verbosity = props.getProperty("verbosity","").toLowerCase().equals("true");

    }
    // methods retrieving props jedai parameters
    public SimilarityMetric getSimilarity(){
        String similarity_str = props.getProperty("similarity");
        if(similarity_str.toLowerCase().equals("jaccard"))
            return SimilarityMetric.JACCARD_SIMILARITY;
        else if(similarity_str.toLowerCase().equals("cosine"))
            return SimilarityMetric.COSINE_SIMILARITY;
        else{
            System.err.println("Undefined similarity param:" + similarity_str);
            return null;
        }
    }
    public String getReadMode(){
        return props.getProperty("read_mode","");
    }
    public boolean getVerbosity(){
        return verbosity;
    }
    public List<String> getLanguages(){
        String langs = props.getProperty("languages","");
        if (langs.isEmpty()) return new ArrayList<>();
        String [] parts = langs.trim().split(",");
        for(int i=0;i<parts.length;++i){
            parts[i] = parts[i].trim();
            verbose("Read lang: [" + parts[i]+"]");
        }
        return Arrays.asList(parts);
    }
    public RepresentationModel getRepresentation(){
        String repr_str = props.getProperty("representation");
        if(repr_str.toLowerCase().equals("bow"))
            return RepresentationModel.TOKEN_UNIGRAMS_TF_IDF;
        else if(repr_str.toLowerCase().equals("ngg"))
            return RepresentationModel.TOKEN_TRIGRAM_GRAPHS;
        else{
            System.err.println("Undefined representation param:" + repr_str);
            return null;
        }
    }
    public String getInputFolder(){
        return props.getProperty("input_folder","");
    }
    public String getReadOrder(){
        return props.getProperty("read_order","");
    }

}
