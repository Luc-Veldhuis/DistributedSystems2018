import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Used to store handy functions
 */
public class Utils {

    /**
     * Return a list of length x
     * @param x
     * @return
     */
    public static List<Integer> getListOfLength(int x) {
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < x; i++) {
            list.add(i);
        }
        return list;
    }


    public static Queue<Job> jobQueue = new ConcurrentLinkedQueue<Job>();

    public static void runNextJob() {
        Job j;
        synchronized(Utils.class) {
            j = jobQueue.poll();
            if(j == null) {
                System.out.println("No new jobs to run.");
                return;
            }

        }

        try {
            j.run();
            } catch (Exception e) {
                System.out.println("Exception inside run job: "+e.toString());
            }

    }

}
