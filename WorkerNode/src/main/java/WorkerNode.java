import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkerNode extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    public Integer workerId;
    public List<ActorSelection> headnodes;

    public static Props props(String[] headnodes) {
        return Props.create(WorkerNode.class, () -> new WorkerNode(headnodes));
    }

    /**
     * A WorkerNode recieves a JobHandler and runs the corresponding function, then returns the JobHandler again with updated results.
     * @param headnodes list of headnode urls
     */
    public WorkerNode(String[] headnodes) {
        log.info("Workernode created");
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
        log.info("Worker " +workerId+" "+"done with job "+job.getId()+" at "+System.currentTimeMillis());
    }

    /**
     * Run the JobHandler
     * @param message
     */
    public void executeJob(GetJobFromHead message) throws GracefulFailureException, UngracefulFailureException {
        log.info("Worker " +workerId+" "+"received job "+message.job.getId()+" at "+System.currentTimeMillis());
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
            throw new GracefulFailureException("Failure");//Restart node
        }
        if(job.numberOfFailSilentFailures == 1) {
            try {
                Thread.sleep(5000);//Simulate timeout
            } catch (InterruptedException e) {
                System.out.println("Sleep interrupted");
            }
            throw new UngracefulFailureException("Silent");//Stop node
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
        log.info("Workernode " + workerId + " registered");
    }

    /**
     * When it goes down, send a message to the HeadNodes
     */
    public void sendRemove() {
        for(ActorSelection node:headnodes){
            node.tell(new RemoveWorkerFromHead(createMessageData()), this.self());
        }
    }

    @Override
    public void postStop() throws Exception {
        log.info("Worker " +workerId+" "+"silent failing at "+System.currentTimeMillis());
        super.postStop();
    }

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
        sendRemove();
        log.info("Worker " +workerId+" "+"stop failing at "+System.currentTimeMillis());
        super.preRestart(reason, message);
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