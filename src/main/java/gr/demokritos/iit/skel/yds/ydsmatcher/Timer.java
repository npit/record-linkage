package gr.demokritos.iit.skel.yds.ydsmatcher;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Timer {
    static final HashMap<String,Long> times = new HashMap<>();
    public static String tic(String msg){
        if (times.containsKey(msg)){
            System.err.println("Already contain a tic with header " + msg + "!");
        }
        times.put(msg, System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        return cal.getTime().toString();

    }

    public static void peek(String msg){
        long msec = tocPeek(msg);
        String tdiff = tostr(msec);
        System.err.println(String.format("Elapsed for [%s] : %s", msg, tdiff ));
    }

    public static Long tocPeek(String msg){
        if (! times.containsKey(msg)){
            System.err.println("No tic with header " + msg + "!");
            return 0l;
        }
        Long diff = System.currentTimeMillis() - times.get(msg);
        // return milliseconds
        return diff;

    }

    public static Long toc(String msg){
        long diff = tocPeek(msg);
        times.remove(msg);
        // return milliseconds
        return diff;
    }

    public static void tictell(String msg){

        System.err.println("Starting timing: " + msg + "  [" + tic(msg) + "]");
    }

    public static void tell(String msg){
        long msec = toc(msg);
        String tdiff = tostr(msec);
        System.err.println(String.format("Elapsed for [%s] : %s", msg, tdiff ));
    }

    public static String tostr(long msecs){
        if (msecs < 1000) return String.format("%d msecs", msecs);
        long secs = msecs / 1000;
        msecs = msecs % 1000;
        if (secs < 60) return String.format("%d secs, %d msecs",secs, msecs);
        long mins =  secs / 60;
        secs = secs % 60;
        if (mins < 60) return String.format("%d mins %d secs, %d msecs",mins, secs, msecs);
        String str =  Long.toString(secs);
        return str;
    }
}
