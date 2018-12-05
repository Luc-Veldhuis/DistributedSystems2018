import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


public class HeadNode extends AbstractActor {

    public final Integer headNodeId;
    public static List<ActorRef> headNodes;
    public Messages messages;
    public static Map<ActorRef, Boolean> workerAvailability;
    public static List<ActorRef> workerNodes;
    //public ClientActor clientActor;
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
        //this.clientActor = new ClientActor();
        this.scheduler = new Scheduler(this.state, this.self());
        this.failCheck = new ByzantianChecker(this.state);
        workerAvailability = new HashMap<>();
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
        System.out.println("Schedule job");
        if(this.isBoss) {
            try {
                this.scheduler.update(message.jobHandler, message.clientActor);
                System.out.println("Schedule job");
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
    public void receivedJob(ClientActor.MessageFromClientToHead clientActor) {
        //List headNodes
        List<ActorRef> heads;
        //testing message
        System.out.println("Message tested success");
        System.out.println("Job Received in headNode");

        state.push(clientActor.job);
        System.out.println("headnodes.get(0) = "+ headNodes.get(0));

        scheduler.getSchedule(state, headNodes);
        //this.executeJob(clientActor.job);
        //send job to the
        this.assignJobToWorker(clientActor.job, workerNodes.get(0));
    }

    public void assignJobToWorker(Job job, ActorRef workerNode){
        this.workerAvailability.replace(workerNode, Boolean.FALSE);
        MessageFromHeadNodeToWorker m = new MessageFromHeadNodeToWorker(job);
        workerNode.tell( m, ActorRef.noSender());
    }


    public static void acceptWorker(List<ActorRef> workerNode){
        System.out.println("acceptWorker()");
        workerNodes = workerNode;
        for(ActorRef node: workerNodes){
            workerAvailability.put(node, Boolean.TRUE);
        }
    }

    public static void getListofHeads(List<ActorRef> headN){
        System.out.println("getListofHeads()");
        headNodes = headN;

        //System.out.println("headNodes.get(0): " + headNodes.get(0));
        //return headNodes;

    }

    public void receiveWorkerResult(WorkerNode.JobExecutionResult result){
        this.workerAvailability.replace(result.getRef(), Boolean.FALSE);
        System.out.println("RESULT: " + result.getResult());
    }

    public void executeJob(Job job){
        job.run();
    }

    @Override
    public void postStop() {
        //graceful failure, alert other headnodes
      /*  if(headNodes == null) {
            return;
        }
        for(ActorRef node : headNodes) {
            if(!this.self().equals(node)) {
                node.tell(this.messages.crashingHeadNode(this), this.self());
            }
        }*/
    }



    public class MessageFromHeadNodeToWorker{
        Job job;
        MessageFromHeadNodeToWorker(Job job){
            this.job  = job;
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
                .match(
                        ClientActor.MessageFromClientToHead.class, this::receivedJob
                )
                .match(
                        WorkerNode.JobExecutionResult.class, this::receiveWorkerResult
                )
                .build();
    }

}