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

  public static Props props(Integer[] headNodeId, Integer[] workerId, Integer numberOfNodes, Integer numberOfWorkers) {

    return Props.create(JobHandler.class, () -> new JobHandler(headNodeId, workerId, numberOfWorkers));
  }

   public JobHandler(Integer[] headNodeId, Integer[] workerId,  Integer numberOfWorkers) {
    this.headNodeId = headNodeId;
    this.workerId = workerId;
    this.numberOfWorkers = numberOfWorkers;
  }

  public static final class DeviceRegistered {
  }

  final Map<Integer, ActorRef> headNodeMap = new HashMap<>();
  final Map<Integer, ActorRef> workerMap = new HashMap<>();

  @Override
  public void preStart() {
    System.out.println("JobHandler started");
    //log.info("JobHandler started");
  }

  @Override
  public void postStop() {
    //log.info("JobHandler stopped");
  }

  private void createHeadNodes(Message message){
    if(headNodeMap.containsKey(this.headNodeId) && workerMap.containsKey(this.workerId)){
      System.out.println("i am not empty bitches");
    }
    else{
      for (int i = 0; i < headNodeId.length; i++) {
          ActorRef aRef = getContext().actorOf(HeadNode.props(headNodeId[i], workerId, this.numberOfWorkers), "group-" + headNodeId[i]);
          if(i == 0){
              System.out.println("HeadId: " + headNodeId[i] + " has workerNodes");
              aRef.tell(new HeadNode.Message(), Actor.noSender());
          }
          else{
              System.out.println("HeadId: " + headNodeId[i] + " does not have worker nodes");
          }
      }
      //getContext().watch(headNode);
      //headNode.forward(trackMsg, getContext());
      //headNodeMap.put(headNodeId, headNode);
    }
/*    Actor tempHeadMap = headNodeMap.get(headNodeId);

    if (tempheadMap != null) {
      tempHeadMap.forward(trackMsg, getContext());*/
  }

/*  private void onTrackDevice(RequestTrackDevice trackMsg) {
    String headNodeId = trackMsg.headNodeId;
    ActorRef ref = headNodeMap.get(headNodeId);
    if (ref != null) {
      ref.forward(trackMsg, getContext());
    } else {
      System.out.println("Creating device group actor for {}\", headNodeId");
      //log.info("Creating device group actor for {}", headNodeId);
      //ActorRef groupActor = getContext().actorOf(DeviceGroup.props(headNodeId), "group-" + headNodeId);
      //getContext().watch(groupActor);
      //groupActor.forward(trackMsg, getContext());
      //headNodeMap.put(headNodeId, groupActor);
      //workerMap.put(groupActor, headNodeId);
    }
  }*/

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
