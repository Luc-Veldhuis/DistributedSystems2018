import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;

import java.util.List;

public class HeadNode extends AbstractActor {

    public final Integer headNodeId;
    public List<ActorRef> headNodes;
    public Messages messages;
    HeadNodeState state;
    Scheduler scheduler;
    ByzantianChecker failCheck;
    int failingNodes = 0;
    boolean isBoss = false;

    public static Props props(Integer headNodeId) {
        System.out.println("Head node created");
        return Props.create(HeadNode.class, () -> new HeadNode(headNodeId));
    }

    public HeadNode(Integer headNodeId) {
        this.headNodeId = headNodeId;
        System.out.println("headNodeId: " + headNodeId);
        if(headNodeId == 0){
            System.out.println("Head node "+headNodeId+" is the boss");
            this.isBoss = true;
        }
        this.state = new HeadNodeState();
        this.messages = new Messages();
        this.scheduler = new Scheduler(this.state, this.self());
        this.failCheck = new ByzantianChecker(this.state);
    }

    public void registerWorker(Messages.RegisterWorkerToHead message) {
        if(this.isBoss) {
            int workerId = message.workerNode.workerId;
            state.workerIdToWorkerNode.put(workerId, message.workerNode.self());
            state.passiveWorkers.add(workerId);
            System.out.println("Registered worker " + message.workerNode.workerId);
        }
    }

    public void scheduleJob(Messages.GetJobFromClient message) {
        if(this.isBoss) {
            try {
                this.scheduler.update(message.jobHandler, message.clientActor);
            } catch (Exception e) {
                //not initialized
                System.out.println("System was not correctly initialized!!!");
            }
        }
    }

    public void removeWorker(Messages.RemoveWorkerFromHead message) {
        if(this.isBoss) {
            state.workerIdToWorkerNode.remove(message.workerNode.workerId);
            System.out.println("Removed worker " + message.workerNode.workerId);
        }
    }

    public void checkJob(Messages.GetJobFromWorker message) {
        if(this.isBoss) {//only do this on 1 actor
            //TODO return to clientActor but check for byzantian errors first
            if (!this.failCheck.check(message.jobHandler)) {
                //TODO restart job
            }
        }
    }

    public void switchToBackupHeadNode(Messages.CrashingHeadNode message) {
        //another headnode went down, take over the state
        this.state = message.headNode.state;
        failingNodes++;
        if(failingNodes == this.headNodeId) {//because we use incremental ids, this works, might be more cleverly done
            System.out.println("Head node "+headNodeId+" is the boss");
            this.isBoss = true;
        }
    }

    public void updateHeadNodes(Messages.PropagateHeadNodes message) {
        //add more headnodes if they are added incrementally
        this.headNodes = message.headNodes;
    }

    @Override
    public void postStop() {
        //graceful failure, alert other headnodes
        if(headNodes == null) {
            return;
        }
        for(ActorRef node : headNodes) {
            if(!this.self().equals(node)) {
                node.tell(this.messages.crashingHeadNode(this), this.self());
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