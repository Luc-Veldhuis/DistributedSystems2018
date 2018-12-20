import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.*;

public class HeadNodeState implements Serializable {

    public int workerIdCounter = 0;
    Map<Integer, ActorRef> workerIdToWorkerNode; // look up worker node actors by their worker IDs
    List<Integer> activeWorkers; //Lists workers who are executing something
    //WARNING!!! the remove() function works on both integers and Integers, but behaviour is different!!!
    //remove(int i) removes worker id at possition i, while remove(Integer i) removes worker id i.
    List<Integer> passiveWorkers; //Lists workers whe are not executing something

    Map<String, ActorRef> jobClientMapping;
    //For Maximize policy
    Queue<String> jobWaitingQueue;
    Map<String, JobWaiting> jobsWaitingForExecutionResults; //Job has been dispatched to workers, waiting for results

    //For Lockstep policy
    Queue<String> jobHanderQueue;
    Map<String, JobHandler> jobHandlerForExecution; //The job queue, used for the MaximizePolicy, because jobs are run 1 at the time

    /**
     * Used to store any possible variables of the HeadNode concerning the state
     */
    public HeadNodeState() {
        workerIdToWorkerNode = new HashMap<>();
        activeWorkers = new ArrayList<>();
        passiveWorkers = new ArrayList<>();

        jobClientMapping = new HashMap<>();

        jobWaitingQueue = new PriorityQueue<>();
        jobsWaitingForExecutionResults = new HashMap<>();

        jobHandlerForExecution = new HashMap<>();
        jobHanderQueue = new PriorityQueue<>();
    }
}
