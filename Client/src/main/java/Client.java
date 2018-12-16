import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client {

    public String[] headNodes;

    /**
     * Used to  spawn an example client to test the system
     * @param headNodeUri a URI of the location of the head node it should connect to
     */
    public Client(String[] headNodeUri) {
        this.headNodes = headNodeUri;
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
            }
            else {
                timeToSleep = min + (max - min) * r.nextDouble();
            }

            job.setJob((SerializableFunction<Integer, Integer>) Client::sleep, (int)timeToSleep);
            job.setHandler((SerializableConsumer<Integer>) Client::done);
            jobList.add(job);
        }
        return jobList;

    }

    /**
     * Function which executes the client, it creates a new Job to run
     */
    public void runTest(List<Job> list) {
        for(Job job: list){
            try {
                job.run(); //Normal job
            } catch (Exception e) {
                System.out.println("Incomplete setup");
            }
        }
    }

    public static void main(String[] args) {
        if(args.length == 0) {
            throw new Error("Missing argument: head node url");
        }
        System.out.println(args[0]);
        Client client = new Client(args);

        // Normal distribution
        List<Job> list = client.createWorkload(5000,true, 10, 3, 0, 0);
        // Uniform distribution
        //List<Job> list = client.createWorkload(100, false, 0, 0, 5, 15);

        client.runTest(list);
    }

}
