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
        //System.out.println("map: ");
        System.out.println("in scheduler: headNode.get(0) = " + headNodes.get(0));
        Map <ActorRef, Job> map = new HashMap<>();
        map.put(headNodes.get(0), job);
        String prompt = new String("MAP CONTAINS: ");
        for(Map.Entry<ActorRef, Job> entry : map.entrySet()){
            //prompt.concat(entry.getKey() + " " + entry.getValue()+ ";");
            System.out.println(entry.getKey() + "/" + entry.getValue());
        }
       System.out.println(prompt);

        return map;
    }

}
