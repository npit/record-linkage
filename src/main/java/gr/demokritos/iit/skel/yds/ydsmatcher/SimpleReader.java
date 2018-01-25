package gr.demokritos.iit.skel.yds.ydsmatcher;

import DataModel.EntityProfile;
import DataReader.EntityReader.EntityCSVReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SimpleReader {

    public List<EntityProfile> read_data(String path){
        File ff = new File(path);
        if(! ff.exists()){
            System.err.println("Path does not exist!" + path);
            return null;
        }
        if(ff.isDirectory()) return readFolder(ff);
        else return readFile(ff);
    }

    public List<EntityProfile> readFolder(File ff){
        Logger.getAnonymousLogger().info(String.format("Reading folder %s", ff.getAbsolutePath()));
        ArrayList<EntityProfile> elist = new ArrayList<>();
        for (File f : ff.listFiles()){
            if(f.isDirectory()) elist.addAll(readFolder(f));
            else elist.addAll(readFile(f));
        }
       return elist;
    }

    public List<EntityProfile> readFile(File ff){
        if (ff.getName().endsWith(".csv")) return readCsvFile(ff);
        return readTextFile(ff);
    }
    public ArrayList<EntityProfile> readTextFile(File ff){
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
        EntityCSVReader reader = new EntityCSVReader(ff.getAbsolutePath());
        return reader.getEntityProfiles();
    }

}
