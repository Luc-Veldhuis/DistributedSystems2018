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

    /**
     * Function which executes the client, it creates a new Job to run
     */
    public void execute() {
        for(int i = 0; i < 100; i++ ) {
            Job job = new Job(this.headNodes);
            job.setJob((SerializableSupplier<Integer>) Client::sleep);
            job.setHandler((SerializableConsumer<Integer>) Client::done);
            //job.setErrors(0,0,2);
        /*Job job1 = new Job(this.headNodes);
        job1.setJob((SerializableSupplier<Integer>) Client::sleep);
        job1.setHandler((SerializableConsumer<Integer>) Client::done);
        job1.setErrors(0,0,1);

        Job job2 = new Job(this.headNodes);
        job2.setJob((SerializableSupplier<Integer>) Client::sleep);
        job2.setHandler((SerializableConsumer<Integer>) Client::done);
        job2.setErrors(2,0,0);

        Job job3 = new Job(this.headNodes);
        job3.setJob((SerializableSupplier<Integer>) Client::sleep);
        job3.setHandler((SerializableConsumer<Integer>) Client::done);
        job3.setErrors(0,1,0);

        Job job4 = new Job(this.headNodes);
        job4.setJob((SerializableSupplier<Integer>) Client::sleep);
        job4.setHandler((SerializableConsumer<Integer>) Client::done);
        job4.setHeadNodeCrash(0);*/
            try {
                //job4.run();//let the headnode crash
                //Thread.sleep(1000);
                job.run();//Normal job
                //job1.run(); //Will crash 1 random worker node and restart it
                //job2.run(); //Contains 2 byzantian errors
                //job3.run(); //This one will crash the worker with simulated timeout
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
        client.execute();
    }

}
