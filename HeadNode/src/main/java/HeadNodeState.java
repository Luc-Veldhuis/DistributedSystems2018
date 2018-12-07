import akka.actor.ActorRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadNodeState {

    Map<Integer, ActorRef> workerIdToWorkerNode; // look up worker node actors by their worker IDs
    List<Integer> activeWorkers; //Lists workers who are executing something
    List<Integer> passiveWorkers; //Lists workers whe are not executing something

    Map<String, JobWaiting> jobsWaiting; //The job queue!!!!
    Map<String, JobWaiting> jobsReadyForChecking; //Job has been dispatched to workers, waiting for results

    List<String> jobsRunning;//Job is actually copied BYZANTIAN times, so these are COPIES from the jobs with ids: "{original-id}-{child-id}", NOT in hashmap (can be changed)

    public HeadNodeState() {
        workerIdToWorkerNode = new HashMap<>();
        jobsWaiting = new HashMap<>();
        jobsReadyForChecking = new HashMap<>();
        jobsRunning = new ArrayList<>();
        activeWorkers = new ArrayList<>();
        passiveWorkers = new ArrayList<>();
    }
}
