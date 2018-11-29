import akka.actor.AbstractActor;
//import akka.actor.ActorLogging;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import java.util.Map;
import akka.actor.Actor;


public class JobHandler extends AbstractActor {
 // private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  public final Integer[] headNodeId;
  public final Integer[] workerId;
  public final Integer numberOfWorkers;

  public static Props props(Integer[] headNodeId, Integer[] workerId, Integer numberOfWorkers) {

    return Props.create(JobHandler.class, () -> new JobHandler(headNodeId, workerId, numberOfWorkers));
  }

   public JobHandler(Integer[] headNodeId, Integer[] workerId,  Integer numberOfWorkers) {
    this.headNodeId = headNodeId;
    this.workerId = workerId;
    this.numberOfWorkers = numberOfWorkers;
  }

  public static final class DeviceRegistered {
  }

  final Map<Integer, ActorRef> headIdToHeadNode = new HashMap<>(); // look up head node actors by their node IDs
  final Map<ActorRef, Integer> headNodeToHeadId = new HashMap<>(); //remove head node id from the map of existing
                                                                   // head nodes to head node mappings.

  @Override
  public void preStart() {
    System.out.println("JobHandler started");
    //log.info("JobHandler started");
  }

  @Override
  public void postStop() {
    //log.info("JobHandler stopped");
  }

  /*** CreateHeadNode ****/
  /* ---- Creates three head nodes and their corresponding ID ---- */
  private void createHeadNodes(Message message){
    if(headIdToHeadNode.containsKey(this.headNodeId)){
      System.out.println("i am not empty");
    }
    else{
      for (int i = 0; i < headNodeId.length; i++) {
          ActorRef aRef = getContext().actorOf(HeadNode.props(headNodeId[i], workerId, this.numberOfWorkers), "headNodeId-" + headNodeId[i]);
          if(i == 0){
              System.out.println("HeadId: " + headNodeId[i] + " has workerNodes");
              aRef.tell(new HeadNode.Message(), Actor.noSender());
              getContext().watch(aRef);//watch the node in case it dies
              //fill the maps
              headIdToHeadNode.put(headNodeId[i], aRef);
              headNodeToHeadId.put(aRef, headNodeId[i]);
          }
          else{
              System.out.println("HeadId: " + headNodeId[i] + " does not have worker nodes");
          }
      }
    }
  }


  //private void onTerminated(Terminated t) {
    // ActorRef groupActor = t.getActor();
    // String headNodeId = workerMap.get(groupActor);
    // log.info("Device group actor for {} has been terminated", headNodeId);
    // workerMap.remove(groupActor);
    // headNodeMap.remove(headNodeId);
  //}

  static public class Message{
    public Message(){}
  }

  public Receive createReceive() {
    return receiveBuilder()
            .match(String.class, msg -> {
              System.out.println(msg);
            })
            .match(Message.class, this::createHeadNodes)
            .build();
  }

}
