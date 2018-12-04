import akka.actor.ActorRef;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Client {

    public ActorRef headNode;

    public Client(ActorRef headNodeUri) {
        this.headNode = headNodeUri;
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
        Job job = new Job(this.headNode);
        job.setJob((Supplier<Integer>) this::sleep);
        job.setHandler((Consumer<Integer>) this::done);
        try {
            job.run();
        } catch (Exception e) {
            System.out.println("Incomplete setup");
        }
    }

}
