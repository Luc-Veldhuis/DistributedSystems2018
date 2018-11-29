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
    public final Integer[] workerId;
    public final Integer numberOfWorkers;

    public static Props props(Integer headNodeId, Integer[] workerId, Integer numberOfWorkers) {

        System.out.println("Head node created");
        return Props.create(HeadNode.class, () -> new HeadNode(headNodeId, workerId, numberOfWorkers));
    }

    public HeadNode(Integer headNodeId, Integer[] workerId, Integer numberOfWorkers) {
        this.headNodeId = headNodeId;
        this.workerId = workerId;
        this.numberOfWorkers = numberOfWorkers;

        System.out.println("headNodeId: " + headNodeId);

    }

    final Map<Integer, ActorRef> workerIdToWorkerNode = new HashMap<>(); // look up worker node actors by their worker IDs
    final Map<ActorRef, Integer> workerNodeToWorkerId = new HashMap<>(); //remove worker node id from the map of existing
                                                                         // worker nodes to worker node mappings.

    private void createWorkerNodes(Message message){
        if(workerIdToWorkerNode.containsKey(this.workerId)){
            System.out.println("i am not empty");
        }
        else{
            for (int i = 0; i < numberOfWorkers; i++) {
                ActorRef aRef = getContext().actorOf(WorkerNode.props(workerId[i]), "workerId-" + workerId[i]);
                getContext().watch(aRef);
                //fill the maps
                workerIdToWorkerNode.put(workerId[i], aRef);
                workerNodeToWorkerId.put(aRef, workerId[i]);
            }
        }
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