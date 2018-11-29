import akka.actor.ActorRef;

public class Scheduler extends Policy {

    public Scheduler(HeadNodeState state, ActorRef headNode) {
        this.state = state;
        this.headNode = headNode;
    }

}
