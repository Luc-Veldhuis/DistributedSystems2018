import akka.actor.AbstractActor;
//import akka.actor.ActorLogging;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import java.util.Map;

public class HeadNode extends AbstractActor {

    public final Integer headNodeId;
    public final Integer workerId;
    public final Integer numberOfWorkers;

    public static Props props(Integer headNodeId, Integer workerId, Integer numberOfWorkers) {

        System.out.println("Head node created");
        return Props.create(HeadNode.class, () -> new HeadNode(headNodeId, workerId, numberOfWorkers));
    }

    public HeadNode(Integer headNodeId, Integer workerId, Integer numberOfWorkers) {
        this.headNodeId = headNodeId;
        this.workerId = workerId;
        this.numberOfWorkers = numberOfWorkers;

        System.out.println("headNodeId: " + headNodeId);

    }

    private void createWorkerNodes(Message message){
            System.out.println("Creating device group actor for {}\", headNodeId");
            for (int i = 0; i < numberOfWorkers; i++) {
                getContext().actorOf(WorkerNode.props(workerId), "group-" + workerId);
            }
            //getContext().watch(headNode);
            //headNode.forward(trackMsg, getContext());
            //headNodeMap.put(headNodeId, headNode);

/*    Actor tempHeadMap = headNodeMap.get(headNodeId);

    if (tempheadMap != null) {
      tempHeadMap.forward(trackMsg, getContext());*/

        System.out.println("worker hoe" + headNodeId);
    }

    static public class Message{
        public Message(){}
    }


    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(Message.class, this::createWorkerNodes)
                .build();
    }

}