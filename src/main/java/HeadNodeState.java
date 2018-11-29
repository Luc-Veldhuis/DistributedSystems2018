import akka.actor.ActorRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadNodeState {

    Map<Integer, ActorRef> workerIdToWorkerNode; // look up worker node actors by their worker IDs

    List<JobHandler> jobs;

    public HeadNodeState() {
        workerIdToWorkerNode = new HashMap<>();
        jobs = new ArrayList<>();
    }
}
