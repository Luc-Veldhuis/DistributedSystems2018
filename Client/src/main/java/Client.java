public class Client {

    public String headNode;

    /**
     * Used to  spawn an example client to test the system
     * @param headNodeUri a URI of the location of the head node it should connect to
     */
    public Client(String headNodeUri) {
        this.headNode = headNodeUri;
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

    /**
     * Function which executes the client, it creates a new Job to run
     */
    public void execute() {
        Job job = new Job(this.headNode);
        job.setJob((SerializableSupplier<Integer>) Client::sleep);
        job.setHandler((SerializableConsumer<Integer>) Client::done);
        try {
            job.run();
        } catch (Exception e) {
            System.out.println("Incomplete setup");
        }
    }

    public static void main(String[] args) {
        if(args.length == 0) {
            throw new Error("Missing argument: head node url");
        }
        System.out.println(args[0]);
        Client client = new Client(args[0]);
        client.execute();
    }

}
