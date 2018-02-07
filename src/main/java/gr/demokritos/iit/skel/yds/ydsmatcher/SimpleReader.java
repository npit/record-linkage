package gr.demokritos.iit.skel.yds.ydsmatcher;

import DataModel.EntityProfile;
import DataModel.SimilarityPairs;
import DataReader.EntityReader.EntityCSVReader;
import Utilities.Enumerations.SimilarityMetric;
import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

public class SimpleReader {
    List<String> langs;
    boolean verbosity;
    String read_mode ;
    public void verbose(String msg){
        if(verbosity) System.out.println(msg);
    }
    public SimpleReader(List<String> langs, boolean verbosity, String read_mode){
        this.langs = langs;
        this.verbosity = verbosity;
        this.read_mode = read_mode;
    }

    public List<Pair<String,Integer>>  getReadOrder(String read_order_file){
        List<Pair<String,Integer>> readorder = new ArrayList<>();
        if(! new File(read_order_file).exists()){
            System.err.println("Read order path does not exist!" + read_order_file);
            return null;
        }
        try {
            BufferedReader bf = new BufferedReader(new FileReader(read_order_file));
            String line;
            while((line = bf.readLine()) != null){
                String [] parts = line.trim().split(" ");
                readorder.add(new Pair(parts[0], Integer.parseInt(parts[1])));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        verbose("Read " + readorder.size() + " readorder items.");
        // sort in increasing index order
        Collections.sort(readorder, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> stringIntegerPair, Pair<String, Integer> t1) {
                return stringIntegerPair.d2.compareTo(t1.d2);
            }
        });
        return readorder;
    }



    public List<EntityProfile> read_data(String path, String read_order_file){
        List<Pair<String,Integer>>  readorder = getReadOrder(read_order_file);
        List<EntityProfile> elist = new ArrayList<>();

        for(Pair<String,Integer> p : readorder){
            String full_path = path + "/" + p.d1;
            verbose("Reading file "  + (p.d2+1) + "/" + readorder.size() + ", idx: " + p.d2 + ", path " + full_path);
            try {
                elist.addAll(readFile(new File(full_path)));
            }catch(Exception ex){
                System.out.println(ex.getMessage());
            }
            if (elist.size() > 200) break;
        }
        return elist;
    }
    public List<EntityProfile> read_data_randomly(String path){
        File ff = new File(path);
        if(! ff.exists()){
            System.err.println("Path does not exist!" + path);
            return null;
        }
        if(ff.isDirectory()) return readFolder(ff, true);
        else return readFile(ff);
    }

    public List<EntityProfile> readFolder(File ff){
        return readFolder(ff, false);
    }

    public List<EntityProfile> readFolder(File ff, boolean isRoot){
        ArrayList<EntityProfile> elist = new ArrayList<>();
        // filter languages, if at non-root directory
        if(!isRoot && !langs.isEmpty()){
            if(!langs.contains(ff.getName())){
                verbose("Skipping direc lang:[" + ff.getName() + "]");
                return elist;
            }
        }

        if(isRoot)
            verbose("Reading root directory:" + ff.getName());
        else
            verbose("Reading directory:" + ff.getName());

        verbose(String.format("Reading folder %s", ff.getAbsolutePath()));
        for (File f : ff.listFiles()){
            if(f.isDirectory()) elist.addAll(readFolder(f));
            else elist.addAll(readFile(f));
        }
       return elist;
    }

    public List<EntityProfile> readFile(File ff){
        if (read_mode.equals("csv")) return readCsvFile(ff);
        if (read_mode.equals("entities")) return readJsonEntityFile(ff);
        if (read_mode.equals("raw")) return readTextFile(ff);
        System.err.println("Undefined read mode: [" + read_mode + "]");
        return null;
    }
    List<EntityProfile> readJsonEntityFile(File ff){
        verbose("Reader : json entity");
        ArrayList<EntityProfile> elist = new ArrayList<>();
        try{
            BufferedReader bf = new BufferedReader(new FileReader(ff));
            String line;
            while((line = bf.readLine()) != null){
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");

                String code = Integer.toString((parts[0]+parts[1]+parts[2]+parts[3]).hashCode());
                EntityProfile ep = new EntityProfile(code);
                ep.addAttribute("name",parts[0]);
                ep.addAttribute("type",parts[1]);
                ep.addAttribute("offset",parts[2]);
                ep.addAttribute("length",parts[3]);
                elist.add(ep);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return elist;
    }

    public ArrayList<EntityProfile> readTextFile(File ff){
        verbose("Reader : raw text");
        ArrayList<EntityProfile> elist = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
       try{
           BufferedReader bf = new BufferedReader(new FileReader(ff));
           String line;
           while((line = bf.readLine()) != null){
               sb.append(line);
           }
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
       EntityProfile ep = new EntityProfile(ff.getName());
       ep.addAttribute("content", sb.toString());
       elist.add(ep);
       return elist;
    }

    public List<EntityProfile> readCsvFile(File ff) {
        verbose("Reader : csv");
        EntityCSVReader reader = new EntityCSVReader(ff.getAbsolutePath());
        return reader.getEntityProfiles();
    }

    public SimilarityPairs readSimilaritiesFile(String filepath, String readOrderPath, String simfield){
        verbose("Loading existing pairwise comparisons from [" + filepath + "]");

        Timer.tictell("csvread");
        List<String[]> data = null;
        try {
            CSVReader reader = new CSVReader(new FileReader(filepath));
            data = reader.readAll();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data == null) return null;
        System.out.println("Num read:" + data.size());

        Timer.toctell("csvread");

        // get readorder to hashmap, skipping preceeding folder
        HashMap<String,Integer> readorder = new HashMap<>();
        List<Pair<String,Integer>> rdo = getReadOrder(readOrderPath);
        if(rdo == null) return null;
        // for(Pair<String,Integer> p : rdo) readorder.put(p.d1.substring(p.d1.indexOf("/")+1),p.d2);
        for(Pair<String,Integer> p : rdo) readorder.put(p.d1, p.d2);

        // read the similarities file
        List<String> names1 = new ArrayList<>();
        List<String> names2 = new ArrayList<>();
        List<Integer> idxs1 = new ArrayList<>();
        List<Integer> idxs2 = new ArrayList<>();
        List<Double> sims = new ArrayList<>();


        int simFieldIndex =0;
        for(;simFieldIndex <data.get(0).length; ++simFieldIndex ){
            if(data.get(0)[simFieldIndex ].equals(simfield)) break;
        }
        if(simFieldIndex  == data.get(0).length){
            System.err.println("Could not find similarity field:" + simfield +" in the header :" + data.get(0));
        }
        data.remove(0);
        int count = 0;
        Timer.tictell("Parse csv");
        for (String[] datum : data) {
            ++count;
            idxs1.add(readorder.get(datum[0]));
            idxs2.add(readorder.get(datum[1]));
            sims.add(Double.parseDouble(datum[simFieldIndex]));
        }
        Timer.toctell("Parse csv");
        Timer.tictell("Convert to jedai-compatible structs");
        int [] idxs1arr = idxs1.stream().mapToInt(i->i).toArray();
        int [] idxs2arr = idxs2.stream().mapToInt(i->i).toArray();
        double [] simsarr = sims.stream().mapToDouble(i->i).toArray();
        Timer.toctell("Convert to jedai-compatible structs");
        SimilarityPairs sp = new SimilarityPairs(false, new ArrayList<>());
        sp.setEntityIds1(idxs1arr);
        sp.setEntityIds2(idxs2arr);
        sp.setSimilarities(simsarr);
        return sp;

    }

}
