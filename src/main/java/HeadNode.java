import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadNode extends AbstractActor {

    public final Integer headNodeId;
    public List<ActorRef> headNodes;
    public Messages messages;
    HeadNodeState state;
    Scheduler scheduler;
    ByzantianChecker failCheck;

    public static Props props(Integer headNodeId) {
        System.out.println("Head node created");
        return Props.create(HeadNode.class, () -> new HeadNode(headNodeId));
    }

    public HeadNode(Integer headNodeId) {
        this.headNodeId = headNodeId;
        System.out.println("headNodeId: " + headNodeId);
        if(headNodeId == 1){
            System.out.println("I am the boss");
        }
        this.state = new HeadNodeState();
        this.messages = new Messages();
        this.scheduler = new Scheduler(this.state);
        this.failCheck = new ByzantianChecker(this.state);
    }

    public void registerWorker(Messages.RegisterWorkerToHead message) {
        state.workerIdToWorkerNode.put(message.workerNode.workerId, message.workerNode.self());
        System.out.println("Registered worker "+message.workerNode.workerId);
    }

    public void scheduleJob(Messages.GetJobFromClient message) {
        this.scheduler.update(message.jobHandler, message.client);
    }

    public void removeWorker(Messages.RemoveWorkerFromHead message) {
        state.workerIdToWorkerNode.remove(message.workerNode.workerId);
    }

    public void checkJob(Messages.GetJobFromWorker message) {
        //TODO return to client but check for byzantian errors first
        if (!this.failCheck.check(message.jobHandler)) {
            //TODO restart job
        }
    }

    public void switchToBackupHeadNode(Messages.CrashingHeadNode message) {
        this.state = message.headNode.state;
        //TODO not done yet
    }

    public void updateHeadNodes(Messages.PropagateHeadNodes message) {
        this.headNodes = message.headNodes;
    }

    @Override
    public void postStop() {
        //TODO handle graceful failure, alert other headnodes
        if(headNodes == null) {
            return;
        }
        for(int i = 0; i < headNodes.size();i++) {
            ActorRef headNode = headNodes.get(i);
            if(!this.self().equals(headNode)) {
                headNode.tell(this.messages.crashingHeadNode(this), this.self());
            }
        }
    }


    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                        Messages.RegisterWorkerToHead.class, this::registerWorker
                )
                .match(
                        Messages.RemoveWorkerFromHead.class, this::removeWorker
                )
                .match(
                        Messages.GetJobFromClient.class, this::scheduleJob
                )
                .match(
                        Messages.GetJobFromWorker.class, this::checkJob
                )
                .match(
                        Messages.CrashingHeadNode.class, this::switchToBackupHeadNode
                )
                .match(
                        Messages.PropagateHeadNodes.class, this::updateHeadNodes
                )
                .build();
    }

}