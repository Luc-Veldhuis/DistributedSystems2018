import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {

    public String[] headNodes;
    int num_concurrent;

    /**
     * Used to  spawn an example client to test the system
     * @param cmdline a URI of the location of the head node it should connect to and whether byzentine errors are supported
     */
    public Client(String[] cmdline) {
        String[] headnodes = Arrays.copyOf(cmdline, cmdline.length-1);
        this.headNodes = headnodes;

        int num_errors_to_correct = Integer.valueOf(cmdline[cmdline.length -1]);


        num_concurrent = (num_errors_to_correct == 0) ?
                Configuration.NUMBER_OF_WORKERS_PER_SYSTEM*Configuration.NUM_DAS4_WORKERS
                : Configuration.NUMBER_OF_WORKERS_PER_SYSTEM*Configuration.NUM_DAS4_WORKERS/5;

        System.out.println("Num concurrent: "+ num_concurrent);
    }

    /**
     * Function which is executed on the remote system, MUST BE STATIC and return type must override default equals function!!!
     * @return return type E, which supports equals method
     */
    public static Integer sleep() {
        System.out.println("Sleep interupted");
        return 10;
    }

    /**
     * Function which is executed on the remote system, MUST BE STATIC and return type must override default equals function!!!
     * @return return type E, which supports equals method
     */
    public static Integer sleep(Integer timeToWait) {
        long startTime;
        long thisTime = System.currentTimeMillis();
        startTime = thisTime;
        while(true) {
            if ((thisTime - startTime) <= timeToWait) {
                thisTime = System.currentTimeMillis();
            } else {
                return timeToWait;
            }
        }
    }

    /**
     * Function which is executed as handler once the function is done
     *
     * @param result input type E
     */
    public static void done(int result) {
        System.out.println("Result: "+result);
    }

    /**
     * Function which creates a job workload with either normally or uniformly distributed durations
     *
     * @param normalDist determines normal or uniform distribution
     * @param mean mean for normal distribution
     * @param stdev standard deviation for normal distribution
     * @param min minimum value for uniform distribution
     * @param max maximum value for uniform distribution
     * @return List of jobs in the workload
     */
    public List<Job> createWorkload(int size, boolean normalDist, int mean, int stdev, int min, int max) {
        List<Job> jobList = new ArrayList<Job>();
        for (int i = 0; i < size; i++) {
            Job job = new Job(this.headNodes);

            // Use either normal or uniform distribution to select next duration
            Random r = new Random();
            double timeToSleep;
            if (normalDist) {
                timeToSleep = r.nextGaussian()*stdev+mean;
                if(timeToSleep < 0) {
                    timeToSleep = 0;
                }
            }
            else {
                timeToSleep = min + (max - min) * r.nextDouble();
            }

            job.setJob((SerializableFunction<Integer, Integer>) Client::sleep, (int)timeToSleep);
            job.setFinishedFunction((SerializableConsumer<Integer>) Client::done);
            //jobList.add(job);
            Utils.jobQueue.add(job);
        }
        return jobList;

    }

    /**
     * Function which executes the client, it creates a new Job to run
     */
    public void runTest(List<Job> list) {


        for(int i = 0; i < num_concurrent; i++) {
            Utils.runNextJob();
        }


        /*
        for(Job job: list){
            try {
                job.run(); //Normal job
            } catch (Exception e) {
                System.out.println("Incomplete setup");
            }
        }
        */

    }



    public static void main(String[] args) {
        if(args.length == 0) {
            throw new Error("Missing argument: head node url");
        }
        System.out.println(args[0]);
        Client client = new Client(args);

        // Normal distribution
        List<Job> list = client.createWorkload(Configuration.NUMBER_OF_JOBS,true, 8000, 2000, 0, 0);
        // Uniform distribution
        //List<Job> list = client.createWorkload(100, false, 0, 0, 5, 15);

        /*
        List<Job> list = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            Job j = new Job(client.headNodes);
            j.setJob((SerializableFunction<Integer, Integer>) Client::sleep, (int) 2000);
            j.setFinishedFunction((SerializableConsumer<Integer>) Client::done);
            //list.add(j);
            Utils.jobQueue.add(j);
        }
        */

        client.runTest(list);
    }

}
