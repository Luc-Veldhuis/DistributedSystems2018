import akka.actor.ActorRef;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Scheduler extends Policy {

    public Scheduler() {
        //this.state = state;
        //this.headNode = headNode;
    }

    public Map <ActorRef, Job> getSchedule(HeadNodeState state, Map<ActorRef, Boolean> availability, List<ActorRef> workerNodes){
        //System.out.println("getSchedule: " + workerNodes.get(0));
       // for(Job  queueState :state.jobQueue){
            System.out.println("State of queue: " + state.jobQueue);
        //}
        Map <ActorRef, Job> schedule = new HashMap<>();
        if(state.jobQueue.isEmpty()){
            return schedule;
        }

        if(availability.get(workerNodes.get(0))){
            schedule.put(workerNodes.get(0), state.pop());
            return schedule;
        }

        return schedule;
    }
}
