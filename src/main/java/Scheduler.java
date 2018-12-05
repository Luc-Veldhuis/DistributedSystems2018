import akka.actor.ActorRef;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Scheduler extends Policy {

    public Scheduler(HeadNodeState state, ActorRef headNode) {
        this.state = state;
        this.headNode = headNode;
    }

    public Map <ActorRef, Job> getSchedule(HeadNodeState state, List<ActorRef> headNodes){

        Job job  =  state.pop();
        System.out.println("map: ");
        Map <ActorRef, Job> map = new HashMap<>();
        map.put(headNodes.get(0), job);

        return map;
    }

}
