import akka.actor.ActorRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;
public class HeadNodeState {
    Map<Integer, ActorRef> workerIdToWorkerNode; // look up worker node actors by their worker IDs
    List<Integer> activeWorkers; //Lists workers who are executing something
    List<Integer> passiveWorkers; //Lists workers whe are not executing something




    List<String> jobsRunning;//Job is actually copied BYZANTIAN times, so these are COPIES from the jobs with ids: "{original-id
    /*--------*/
    Queue<Job> jobQueue; //job queue

    public HeadNodeState(){
        jobQueue = new LinkedList<Job>();


        workerIdToWorkerNode = new HashMap<>();

        jobsRunning = new ArrayList<>();
        activeWorkers = new ArrayList<>();
        passiveWorkers = new ArrayList<>();
    }

    //pop thr queue
    public Job pop(){
        Job job = jobQueue.remove();
        System.out.println("pop: " + job);
        return job;
    }

    //push to the queue
    public void push(Job job){
        jobQueue.add(job);

        System.out.println("push: " + jobQueue);
    }

    }

    /*public HeadNodeState() {
        workerIdToWorkerNode = new HashMap<>();
        jobsWaiting = new HashMap<>();
        jobsReadyForChecking = new HashMap<>();
        jobsRunning = new ArrayList<>();
        activeWorkers = new ArrayList<>();
        passiveWorkers = new ArrayList<>();
    }*/

