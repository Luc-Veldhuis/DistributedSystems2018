import java.util.function.Consumer;
import java.util.function.Supplier;

public class Client {

    public String headNode;

    public Client(String headNodeUri) {
        this.headNode = headNodeUri;
    }

    public static Integer sleep() {
        System.out.println("Sleep interupted");
        return 10;
    }

    public static void done(int result) {
        System.out.println(result);
    }

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
