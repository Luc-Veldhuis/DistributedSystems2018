import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;

import java.util.List;

public class HeadNode extends AbstractActor {

    public final Integer headNodeId;
    public List<ActorRef> headNodes;
    /**
     * Any stuff relating to the state of the node should be stored in this class.
     */
    public HeadNodeState state;
    Scheduler scheduler;
    ByzantianChecker failCheck;
    int failingNodes = 0;
    boolean isBoss = false;

    /**
     * This actor handles all logic in scheduling, receiving and delegating jobs.
     * @param headNodeId
     * @return
     */
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
        this.scheduler = new Scheduler(this.state, this.self());
        this.failCheck = new ByzantianChecker(this.state);
    }

    /**
     * Call used to register a WorkerNode to the HeadNode
     * @param message
     */
    public void registerWorker(WorkerNode.RegisterWorkerToHead message) {
        if(this.isBoss) {
            int workerId = message.workerNode.workerId;
            state.workerIdToWorkerNode.put(workerId, message.workerNode.self);
            state.passiveWorkers.add(workerId);
            System.out.println("Registered worker " + message.workerNode.workerId);
        }
    }

    /**
     * Call used when a Job is send by the client
     * @param message
     */
    public void scheduleJob(JobActor.GetJobFromClient message) {
        if(this.isBoss) {
            //this.scheduler.update(message.jobHandler, actor);
        }
    }

    /**
     * Call used when a WorkerNode leaves the system
     * @param message
     */
    public void removeWorker(WorkerNode.RemoveWorkerFromHead message) {
        if(this.isBoss) {
            state.workerIdToWorkerNode.remove(message.workerNode.workerId);
            System.out.println("Removed worker " + message.workerNode.workerId);
        }
    }

    /**
     * Once a job has run x times, check if there are any Byzantine errors
     * @param message
     */
    public void checkJob(WorkerNode.SendJobDone message) {
        if(this.isBoss) {//only do this on 1 actor
            //TODO return to jobActor but check for byzantian errors first
            if (!this.failCheck.check(message.jobHandler)) {
                //TODO restart job
            }
        }
    }

    /**
     * Call when 1 of the head nodes has failed, it receives the HeadNodeState
     * @param message
     */
    public void switchToBackupHeadNode(HeadNode.CrashingHeadNode message) {
        //another headnode went down, take over the state
        this.state = message.headNode.state;
        failingNodes++;
        if(failingNodes == this.headNodeId) {//because we use incremental ids, this works, might be more cleverly done
            System.out.println("Head node "+headNodeId+" is the boss");
            this.isBoss = true;
        }
    }

    /**
     * Call to receive the location of the other HeadNodes, should be received from the SpawnHeadNodes class
     * @param message
     */
    public void updateHeadNodes(HeadNode.PropagateHeadNodes message) {
        //TODO never send!!!
        //add more headnodes if they are added incrementally
        this.headNodes = message.headNodes;
    }

    /**
     * Used for gracefull failure, send the state to the other HeadNodes
     */
    @Override
    public void postStop() {
        //graceful failure, alert other headnodes
        if(headNodes == null) {
            return;
        }
        for(ActorRef node : headNodes) {
            if(!this.self().equals(node)) {
                node.tell(new HeadNode.CrashingHeadNode(this), this.self());
            }
        }
    }


    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                        WorkerNode.RegisterWorkerToHead.class, this::registerWorker
                )
                .match(
                        WorkerNode.RemoveWorkerFromHead.class, this::removeWorker
                )
                .match(
                        JobActor.GetJobFromClient.class, this::scheduleJob
                )
                .match(
                        WorkerNode.SendJobDone.class, this::checkJob
                )
                .match(
                        HeadNode.CrashingHeadNode.class, this::switchToBackupHeadNode
                )
                .match(
                        HeadNode.PropagateHeadNodes.class, this::updateHeadNodes
                )
                .build();
    }

    /**
     * Message to receive when all HeadNodes are online
     */
    public class PropagateHeadNodes {
        public List<ActorRef> headNodes;
        public PropagateHeadNodes(List<ActorRef> headNodes) {
            this.headNodes = headNodes;
        }
    }

    /**
     * Message to send when the HeadNode crashes
     */
    public class CrashingHeadNode {
        public HeadNode headNode;
        public CrashingHeadNode(HeadNode headNode) {
            this.headNode = headNode;
        }
    }

}