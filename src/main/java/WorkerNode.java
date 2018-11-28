import akka.actor.AbstractActor;
//import akka.actor.ActorLogging;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import java.util.Map;

public class WorkerNode extends AbstractActor {
    public final Integer workerId;

    public static Props props(Integer workerId) {

        System.out.println("Worker node created");
        return Props.create(WorkerNode.class, () -> new WorkerNode(workerId));
    }

    public WorkerNode(Integer workerId) {
        this.workerId = workerId;

        System.out.println("workerNodeId: " + workerId);

    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                }).build();
    }
}