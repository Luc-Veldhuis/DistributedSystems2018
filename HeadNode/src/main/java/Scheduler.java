import akka.actor.ActorRef;

public class Scheduler extends Policy {

    /**
     * Wrapper class for the policy, choise of policy should be made here.
     * @param state
     * @param headNode
     */
    public Scheduler(HeadNodeState state, ActorRef headNode) {
        this.state = state;
        this.headNode = headNode;
    }

}
