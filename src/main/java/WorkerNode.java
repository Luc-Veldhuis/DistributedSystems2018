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
        this.result = new JobExecutionResult();

        System.out.println("workerNodeId: " + workerId);

        if(headnodes.size() > 1) {
            registerWorker();
        }
    }

    public void sendResult(JobExecutionResult result) {
        for(ActorRef node:headnodes) {
            node.tell( this.result, getSelf());
        }
    }

    public void executeJob(Messages.SendJobToWorker message) {
        try {
            message.job.setResult(message.job.job.get());
        }  catch (Exception e) {
            message.job.setException(e);
        }
        sendResult(this.result);
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
        this.executeJob(headActor.job);
    }

    public void executeJob(Job job){
        this.result.setResult( job.run(), getSelf());

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
               /* .match(
                        Messages.SendJobToWorker.class, this::executeJob
                )*/
                .match(
                        HeadNode.MessageFromHeadNodeToWorker.class, this::receiveJob
                )
                .build();
    }
}