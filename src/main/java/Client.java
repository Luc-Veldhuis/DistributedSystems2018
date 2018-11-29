import akka.actor.ActorRef;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Client {

    public ActorRef headNode;

    public Client(ActorRef headNode) {
        this.headNode = headNode;
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
    }

    public static void main(String[] args) {
        ActorRef headNode = null;//TODO create actorRef from url
        Client client = new Client(headNode);
        client.execute();
    }
}
