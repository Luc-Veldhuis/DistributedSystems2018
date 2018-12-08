import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;
import java.util.List;

public class WorkerNode extends AbstractActor {
    public final Integer workerId;
    public List<ActorRef> headnodes;
    public Messages messages;
    public JobExecutionResult result;

    public static Props props(Integer workerId, List<ActorRef> headnodes) {

        System.out.println("Worker node created");
        return Props.create(WorkerNode.class, () -> new WorkerNode(workerId, headnodes));
    }

    public WorkerNode(Integer workerId, List<ActorRef> headnodes) {
        this.workerId = workerId;
        this.headnodes = headnodes;
        this.messages = new Messages();

        System.out.println("workerNodeId: " + workerId);
        if(headnodes.size() > 1) {
            registerWorker();
        }
    }

    public void sendResult(JobExecutionResult result) {

        for(ActorRef node:headnodes) {
            System.out.println("sendResult headNodes: " + node);
            node.tell(result, ActorRef.noSender());
        }
        System.out.println("sendResult: result " + result.getResult());
    }

    public void registerWorker() {
        for(ActorRef node:headnodes) {
            node.tell(messages.registerWorkerToHead(this), this.self());
        }
    }

    public void sendRemove() {
        //TODO maybe overkill to send it to each headnode
        for(ActorRef node:headnodes){
            node.tell(this.messages.removeWorkerToHead(this), this.self());
        }
    }

    public void receiveJob(HeadNode.MessageFromHeadNodeToWorker headActor) {
        System.out.println("Job Received in workerNode");
        JobExecutionResult result = new JobExecutionResult();
        System.out.println("result: " + result);
        result.setResult(this.executeJob(headActor.job), getSelf());
        sendResult(result);
    }

    public Object executeJob(Job job){
        return job.run();
    }

    @Override
    public void postStop() {
        //sendRemove();
    }

    public class JobExecutionResult{
        Object obj;
        ActorRef ref;
        JobExecutionResult(){
        }

        public void setResult(Object obj, ActorRef ref){
            this.obj  = obj;
            this.ref = ref;
        }

        public Object getResult(){
            return this.obj;
        }

        public ActorRef getRef(){
            return this.ref;
        }
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                        HeadNode.MessageFromHeadNodeToWorker.class, this::receiveJob
                )
                .build();
    }
}