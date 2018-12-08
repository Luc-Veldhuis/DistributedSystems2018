import akka.actor.ActorRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadNodeState {

    Map<Integer, ActorRef> workerIdToWorkerNode; // look up worker node actors by their worker IDs
    List<Integer> activeWorkers; //Lists workers who are executing something
    List<Integer> passiveWorkers; //Lists workers whe are not executing something

    Map<String, JobWaiting> jobsWaitingForExecution; //The job queue!!!!
    Map<String, JobWaiting> jobsWaitingForExecutionResults; //Job has been dispatched to workers, waiting for results

    List<String> jobsRunning;//Job is actually copied BYZANTIAN times, so these are COPIES from the jobs with ids: "{original-id}-{child-id}", NOT in hashmap (can be changed)

    /**
     * Used to store any possible variables of the HeadNode concerning the state
     */
    public HeadNodeState() {
        workerIdToWorkerNode = new HashMap<>();
        jobsWaitingForExecution = new HashMap<>();
        jobsWaitingForExecutionResults = new HashMap<>();
        jobsRunning = new ArrayList<>();
        activeWorkers = new ArrayList<>();
        passiveWorkers = new ArrayList<>();
    }
}
