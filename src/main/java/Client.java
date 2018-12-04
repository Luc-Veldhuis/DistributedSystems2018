import akka.actor.ActorRef;
import akka.actor.ActorSelection;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Client {

    public String[] headNodeUri;

    public Client(String[] headNodeUri) {
        this.headNodeUri = headNodeUri;
    }

    public int sleep() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted");
        }
        return 10;
    }

    public void done(int result) {
        System.out.println(result);
    }

    public void execute() {
        Job job = new Job(this.headNodeUri[0]);
        job.setJob((Supplier<Integer>) this::sleep);
        job.setHandler((Consumer<Integer>) this::done);
        try {
            job.run();
        } catch (Exception e) {
            System.out.println("Incomplete setup");
        }
    }

    public static void main(String[] args) {
        Client client = new Client(args);
        client.execute();
    }
}
