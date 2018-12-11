import java.util.ArrayList;
import java.util.List;

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
     * Function which is executed as handler once the function is done
     *
     * @param result input type E
     */
    public static void done(int result) {
        System.out.println("Result: "+result);
    }

    public List<Job> createWorkload() {
        List<Job> jobList = new ArrayList<Job>();
        for (int i = 0; i < 100; i++) {
            Job job = new Job(this.headNodes);
            job.setJob((SerializableSupplier<Integer>) Client::sleep);
            job.setHandler((SerializableConsumer<Integer>) Client::done);
            jobList.add(job);
        }
        return jobList;

    }

    /**
     * Function which executes the client, it creates a new Job to run
     */
    public void runTest() {
        List<Job> list = createWorkload();
        for(Job job: list){
            try {
                job.run();//Normal job
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
        client.runTest();
    }

}
