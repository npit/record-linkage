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
        verbose("Read mode:" + read_mode);
    }

    public List<Pair<String,Integer>>  getReadOrder(String read_order_file){
        List<Pair<String,Integer>> readorder = new ArrayList<>();
        if(! new File(read_order_file).exists()){
            System.err.println("Read order path does not exist: [" + read_order_file + "]");
            return null;
        }
        try {
            BufferedReader bf = new BufferedReader(new FileReader(read_order_file));
            String line;
            while((line = bf.readLine()) != null){
                String [] parts = line.trim().split(" ");
                readorder.add(new Pair(parts[0], Integer.parseInt(parts[1])));
            }
            bf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        verbose("Read " + readorder.size() + " readorder items from [" + read_order_file + "].");
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

        verbose("Reading data from path:[" + path + "]");
        for(Pair<String,Integer> p : readorder){
            String full_path = path + "/" + p.d1;
            verbose("Reading file "  + (p.d2+1) + "/" + readorder.size() + ", idx: " + p.d2 + ", path " + full_path);
            try {
                elist.addAll(readFile(new File(full_path)));
            }catch(Exception ex){
                System.out.println("Exception: " + ex.getMessage());
            }
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
        if (read_mode.equals("texts")) return readTextFile(ff);
        System.err.println("Undefined read mode: [" + read_mode + "]");
        return null;
    }
    List<EntityProfile> readJsonEntityFile(File ff){
        ArrayList<EntityProfile> elist = new ArrayList<>();
        EntityProfile ep = new EntityProfile(ff.getName());
        try{
            BufferedReader bf = new BufferedReader(new FileReader(ff));
            String line;
            while((line = bf.readLine()) != null){
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                // keep only hashed values of name & type
                String hash = Integer.toString(parts[0].hashCode());
                ep.addAttribute("name",hash);
                if (parts.length > 1){
                    hash = Integer.toString(parts[1].hashCode());
                    ep.addAttribute("type",hash);
                }

            }
            bf.close();
        } catch (FileNotFoundException e) {
            System.err.println("Entity reading exception:" + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("Entity reading exception:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
        catch(Exception e){
            System.err.println("Entity reading exception:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
        elist.add(ep);
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
           bf.close();
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
        verbose("Num data read from similarities file: " + data.size());

        Timer.tell("csvread");

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
            System.err.print("Could not find similarity field:" + simfield +" in the header :");
            for(String s: data.get(0)) System.out.print(s + " ");
            System.out.println();

        }
        data.remove(0);
        int count = 0;
        Timer.tictell("Parse csv");
        for (String[] datum : data) {
            ++count;
            if(!readorder.containsKey(datum[0])){
                System.err.println("Missing from readorder : [" + datum[0] + "]");
                return null;
            }
            if(!readorder.containsKey(datum[1])){
                System.err.println("Missing from readorder : [" + datum[1] + "]");
                return null;
            }
            idxs1.add(readorder.get(datum[0]));
            idxs2.add(readorder.get(datum[1]));
            try{
                sims.add(Double.parseDouble(datum[simFieldIndex]));
            }catch(NumberFormatException ex){
                System.err.println("Failed to parse double from : [" + datum[simFieldIndex] + "]");
                return null;
                
            }
        }
        verbose("Read idx1, idx2, sims: " + idxs1.size() + ", " + idxs2.size() + ", " + sims.size() );
        Timer.tell("Parse csv");
        Timer.tictell("Convert to jedai-compatible structs");
        try{
            int [] idxs1arr = new int[idxs1.size()];
            int [] idxs2arr = new int[idxs2.size()];
            double [] simsarr = new double[sims.size()];
            for(int i=0;i<idxs1.size();++i) idxs1arr[i] = idxs1.get(i);
            for(int i=0;i<idxs2.size();++i) idxs2arr[i] = idxs2.get(i);
            for(int i=0;i<sims.size();++i) simsarr[i] = sims.get(i);
            Timer.tell("Convert to jedai-compatible structs");
            SimilarityPairs sp = new SimilarityPairs(false, new ArrayList<>());
            sp.setEntityIds1(idxs1arr);
            sp.setEntityIds2(idxs2arr);
            sp.setSimilarities(simsarr);
            return sp;
        }
        catch(OutOfMemoryError ex){
            System.err.println("Out of memory:" + ex.getMessage());
            ex.printStackTrace();
        }catch(Exception ex){
            System.err.println("Error:" + ex.getMessage());
            ex.printStackTrace();
        }
        return null;

    }

}
