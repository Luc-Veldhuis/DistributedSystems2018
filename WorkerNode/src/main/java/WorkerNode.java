import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkerNode extends AbstractActor {
    public Integer workerId;
    public List<ActorSelection> headnodes;

    //For debugging
    public boolean isSilent = true;

    public static Props props(String[] headnodes) {

        System.out.println("Worker node created");
        return Props.create(WorkerNode.class, () -> new WorkerNode(headnodes));
    }

    /**
     * A WorkerNode recieves a JobHandler and runs the corresponding function, then returns the JobHandler again with updated results.
     * @param headnodes list of headnode urls
     */
    public WorkerNode(String[] headnodes) {
        this.headnodes = new ArrayList<>();
        for(String node : headnodes) {
            this.headnodes.add(getContext().actorSelection(node));
        }
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
    public void executeJob(GetJobFromHead message) throws GracefulFailureException, UngracefulFailureException {
        if(processFailures(message.job)) {
            sendResult(message.job);
            return;
        }
        try {
            message.job.setResult(message.job.job.get());
        }  catch (Exception e) {
            message.job.setException(e);
        }
        sendResult(message.job);
    }

    private boolean processFailures(JobHandler job) throws GracefulFailureException, UngracefulFailureException {
        boolean inducedError = false;
        if(job.numberOfFailStopFailures == 1) {
            isSilent = false;
            throw new GracefulFailureException("Failure");
        }
        if(job.numberOfFailSilentFailures == 1) {
            isSilent = true;
            throw new UngracefulFailureException("Silent");
        }
        if(job.numberOfByzantianFailures == 1) {
            inducedError = true;
            job.setResult((Integer)(int)Math.random());

        }
        return inducedError;
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
     * Receives message from the head with id to use
     * @param message Message containing the ID
     */
    public void getAssignedId(GetRegistrationResult message) {
        this.workerId = message.workerId;
        System.out.println("workerNodeId: " + workerId);
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
        if(!isSilent) {
            System.out.println("Sending removing worker " + workerId);
            sendRemove();
        }
    }


    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                        GetJobFromHead.class, this::executeJob
                )
                .match(
                        GetRegistrationResult.class, this::getAssignedId
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
     * Message send from the HeadNode to assign a unique ID
     */
    public static class GetRegistrationResult implements Serializable {
        Integer workerId;

        public GetRegistrationResult(Integer id) {
            this.workerId = id;
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