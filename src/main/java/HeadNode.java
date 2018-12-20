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
        this.scheduler = new Scheduler();
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


    public void removeWorker(Messages.RemoveWorkerFromHead message) {
        if(this.isBoss) {
            state.workerIdToWorkerNode.remove(message.workerNode.workerId);
            System.out.println("Removed worker " + message.workerNode.workerId);
        }
    }

    public void receivedJob(ClientActor.MessageFromClientToHead clientActor) {
        //List headNodes
        List<ActorRef> heads;
        //testing message
        System.out.println("Message tested success");
        System.out.println("Job Received in headNode");

        state.push(clientActor.job);
        System.out.println("headnodes.get(0) = "+ headNodes.get(0));

        //scheduler.getSchedule(state, workerAvailability, headNodes);
        //if(scheduler.getSchedule(state, workerAvailability, headNodes) == null) System.out.println("schedule is null");

        Map <ActorRef, Job> schedule = new HashMap<>();
        schedule = scheduler.getSchedule(state, workerAvailability, workerNodes);
        if( schedule.isEmpty() == false){
            for( Map.Entry<ActorRef, Job> entry : schedule.entrySet()){
                System.out.println("SCHEDULE: " + entry.getValue() +" " + entry.getKey());
                this.assignJobToWorker(entry.getValue(), entry.getKey());
            }
        }

        //this.executeJob(clientActor.job);
        //send job to the
        //this.assignJobToWorker(clientActor.job, workerNodes.get(0));
    }

    public void assignJobToWorker(Job job, ActorRef workerNode){
        this.workerAvailability.replace(workerNode, Boolean.FALSE);
        MessageFromHeadNodeToWorker m = new MessageFromHeadNodeToWorker(job);
        workerNode.tell( m, ActorRef.noSender());
    }

    public static void getListofWorkers(List<ActorRef> workerNode){
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
        this.workerAvailability.replace(result.getRef(), Boolean.TRUE);
        //printavailability();

        if(this.isBoss) {

            Map <ActorRef, Job> schedule = new HashMap<>();
            schedule = scheduler.getSchedule(state, workerAvailability, workerNodes);
            if( schedule.isEmpty() == false){
                for( Map.Entry<ActorRef, Job> entry : schedule.entrySet()){
                    System.out.println("SCHEDULE: " + entry.getValue() +" " + entry.getKey());
                    this.assignJobToWorker(entry.getValue(), entry.getKey());
                }
            }

            System.out.println("RESULT:  arrived + " + result.getResult());
        }
    }

    private void printavailability(){
        /*for(Map.Entry<ActorRef, Boolean> entry : workerAvailability.entrySet()){
            //prompt.concat(entry.getKey() + " " + entry.getValue()+ ";");
            System.out.println(entry.getKey() + "/" + entry.getValue());
        }*/System.out.println( "AVAILABILITY: " + workerAvailability);
    }

    public void executeJob(Job job){
        job.run();
    }

    @Override
    public void postStop() {
        //graceful failure, alert other headnodes
      /*  if(headNodes == null) {
            return;p[loi
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
                        ClientActor.MessageFromClientToHead.class, this::receivedJob
                )
                .match(
                        WorkerNode.JobExecutionResult.class, this::receiveWorkerResult
                )
                .build();
    }

}