import akka.actor.AbstractActor;
//import akka.actor.ActorLogging;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import java.util.Map;


public class JobHandler extends AbstractActor {
 // private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public static Props props() {
    return Props.create(JobHandler.class, JobHandler::new);
  }

  public static final class RequestTrackDevice {
    public final String groupId;
    public final String deviceId;

    public RequestTrackDevice(String groupId, String deviceId) {
      this.groupId = groupId;
      this.deviceId = deviceId;
    }
  }

  public static final class DeviceRegistered {
  }

  final Map<String, ActorRef> headNodeMap = new HashMap<>();
  final Map<ActorRef, String> workerMap = new HashMap<>();

  @Override
  public void preStart() {
    System.out.println("JobHandler started");
    //log.info("JobHandler started");
  }

  @Override
  public void postStop() {
    //log.info("JobHandler stopped");
  }

  private void onTrackDevice(RequestTrackDevice trackMsg) {
    String groupId = trackMsg.groupId;
    ActorRef ref = headNodeMap.get(groupId);
    if (ref != null) {
      ref.forward(trackMsg, getContext());
    } else {
      System.out.println("Creating device group actor for {}\", groupId");
      //log.info("Creating device group actor for {}", groupId);
      //ActorRef groupActor = getContext().actorOf(DeviceGroup.props(groupId), "group-" + groupId);
      //getContext().watch(groupActor);
      //groupActor.forward(trackMsg, getContext());
      //headNodeMap.put(groupId, groupActor);
      //workerMap.put(groupActor, groupId);
    }
  }

  //private void onTerminated(Terminated t) {
    // ActorRef groupActor = t.getActor();
    // String groupId = workerMap.get(groupActor);
    // log.info("Device group actor for {} has been terminated", groupId);
    // workerMap.remove(groupActor);
    // headNodeMap.remove(groupId);
  //}

  public Receive createReceive() {
    return receiveBuilder()
            .match(String.class, msg -> {
              System.out.println(msg);
            }).build();
  }

}