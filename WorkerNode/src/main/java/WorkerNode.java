import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkerNode extends AbstractActor {
    public final Integer workerId;
    public List<ActorSelection> headnodes;

    public static Props props(Integer workerId, String[] headnodes) {

        System.out.println("Worker node created");
        return Props.create(WorkerNode.class, () -> new WorkerNode(workerId, headnodes));
    }

    /**
     * A WorkerNode recieves a JobHandler and runs the corresponding function, then returns the JobHandler again with updated results.
     * @param workerId the id of the worker
     * @param headnodes list of headnode urls
     */
    public WorkerNode(Integer workerId, String[] headnodes) {
        this.workerId = workerId;
        this.headnodes = new ArrayList<>();
        for(String node : headnodes) {
            this.headnodes.add(getContext().actorSelection(node));
        }

        System.out.println("workerNodeId: " + workerId);
        if(this.headnodes.size() >= 1) {
            registerWorker();
        }
    }

    /**
     * It was hard to do the referencing right, so I had to create the separte class to prevent double inclusions (Head requires Worker, Worker requires Head)
     * This function quickly creates this Class which carries data.
     * @return
     */
    WorkerData createMessageData() {
        return new WorkerData(this.getSelf(), this.workerId);
    }

    /**
     * Send the result back to the HeadNodes
     * @param job
     */
    public void sendResult(JobHandler job) {
        for(ActorSelection node:headnodes) {
            node.tell(new SendJobDone(job,createMessageData()), this.self());
        }
    }

    /**
     * Run the JobHandler
     * @param message
     */
    public void executeJob(GetJobFromHead message) {
        try {
            message.job.setResult(message.job.job.get());
        }  catch (Exception e) {
            message.job.setException(e);
        }
        sendResult(message.job);
    }

    /**
     * Used to register the worker when it comes online
     */
    public void registerWorker() {
        for(ActorSelection node:headnodes) {
            node.tell(new RegisterWorkerToHead(createMessageData()), this.self());
        }
    }

    /**
     * When it goes down, send a message to the HeadNodes
     */
    public void sendRemove() {
        for(ActorSelection node:headnodes){
            node.tell(new RemoveWorkerFromHead(createMessageData()), this.self());
        }
    }

    /**
     * Called when the Actor goes woen
     */
    @Override
    public void postStop() {
        sendRemove();
    }


    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                        GetJobFromHead.class, this::executeJob
                )
                .build();
    }

    /**
     * Message send to the HeadNode when the Actor is created
     */
    public static class RegisterWorkerToHead implements Serializable {
        public WorkerData workerNode;

        public RegisterWorkerToHead(WorkerData worker) {
            this.workerNode = worker;
        }
    }

    /**
     * Message send to the HeadNode when the Actor is destroyed
     */
    public static class RemoveWorkerFromHead implements Serializable {
        public WorkerData workerNode;

        public RemoveWorkerFromHead(WorkerData worker) {
            this.workerNode = worker;
        }
    }

    /**
     * Message send from the HeadNode to the WorkerNode to send a JobHandler
     */
    public static class GetJobFromHead implements Serializable {
        public JobHandler job;
        GetJobFromHead(JobHandler job) {
            this.job = job;
        }
    }

    /**
     * Message send from the WorkerNode to the HeadNode with results
     */
    public static class SendJobDone implements Serializable {
        public JobHandler jobHandler;
        public WorkerData workerNode;

        public SendJobDone(JobHandler worker, WorkerData workerNode) {
            this.jobHandler = worker;
            this.workerNode = workerNode;
        }
    }



}