import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class HeadNodeState {

    final Map<Integer, ActorRef> workerIdToWorkerNode; // look up worker node actors by their worker IDs

    public HeadNodeState() {
        workerIdToWorkerNode = new HashMap<>();
    }
}
