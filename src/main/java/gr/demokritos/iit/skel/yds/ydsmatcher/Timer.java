package gr.demokritos.iit.skel.yds.ydsmatcher;

import java.util.ArrayList;
import java.util.HashMap;

public class Timer {
    HashMap<String,Long> times;
    public Timer(){
        times = new HashMap<>();
    }
    public void tic(String msg){
        if (times.containsKey(msg)){
            System.err.println("Already contain a tic with header " + msg + "!");
        }
        times.put(msg, System.currentTimeMillis());
    }
    public Long toc(String msg){
        if (! times.containsKey(msg)){
            System.err.println("Already contain a tic with header " + msg + "!");
            return 0l;
        }
        Long diff = System.currentTimeMillis() - times.get(msg);
        // return seconds
        return diff / 1000;

    }
}
