import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
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
    public boolean silentFailing = false;

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
        //job.finishedTime = System.currentTimeMillis();
        for(ActorSelection node:headnodes) {
            node.tell(new SendJobDone(job,createMessageData()), this.self());
        }
        //log.info("Worker " +workerId+" "+"done with job "+job.getId()+" at "+job.finishedTime);
        log.info("///WORKER-FINISHED-JOB: ("+job.parentId+","+ (System.currentTimeMillis() - job.workerIncomingTimestamp)+ ")"    );

        //log.info("///WORKER-FINISHED-JOB: ("+job.originalCreationTime+","+job.finishedTime+","+ (job.finishedTime - job.originalCreationTime) +"," + job.input +"," + job.parentId.length()+","+job.parentId+")" );
    }

    /**
     * Run the JobHandler
     * @param message
     */
    public void executeJob(GetJobFromHead message) throws GracefulFailureException, UngracefulFailureException {
        message.job.workerIncomingTimestamp = System.currentTimeMillis();

        if(workerId == null) {
            //discard message, not initialized
            return;
        }
        log.info("Worker " +workerId+" "+"received job "+message.job.getId()+" at "+System.currentTimeMillis());
        if(processFailures(message.job)) {
            sendResult(message.job);
            return;
        }
        try {
            if(message.job.job == null) {
                message.job.setResult(message.job.functionJob.apply(message.job.input)); // function
            } else {
                message.job.setResult(message.job.job.get()); //consumer
            }
        } catch (Exception e) {
            message.job.setException(e);
        }
        sendResult(message.job);
    }

    private void simulateTiming(long timeToWait) {
        long startTime;
        long thisTime = System.currentTimeMillis();
        startTime = thisTime;
        while(true) {
            if ((thisTime - startTime) <= timeToWait) {
                thisTime = System.currentTimeMillis();;
            } else {
                return;
            }
        }
    }

    private boolean processFailures(JobHandler job) throws GracefulFailureException, UngracefulFailureException {
        boolean inducedError = false;
        if(job.numberOfByzantianFailures != 0 || job.numberOfFailSilentFailures != 0 || job.numberOfFailStopFailures != 0) {
            simulateTiming((long)(Math.random()*Configuration.MAXIMUM_FAKED_EXECUTION_TIME));//Simulate failure during the job
            inducedError = true;
        }
        if(job.numberOfFailStopFailures == 1) {
            log.info("Stop failure at "+workerId+" at "+System.currentTimeMillis());
            log.info("///////WORKER-STOP-FAILURE: ("+job.parentId+","+ (System.currentTimeMillis() - job.workerIncomingTimestamp)+ ")"    );
            throw new GracefulFailureException("Failure");//Restart node
        }
        if(job.numberOfFailSilentFailures == 1) {
            simulateTiming(Configuration.TIMEOUT_DETECTION_TIME);
            silentFailing = true;
            log.info("Silent failure at "+workerId+" at "+System.currentTimeMillis());
            log.info("///////WORKER-SILENT-FAILURE: ("+job.parentId+","+ (System.currentTimeMillis() - job.workerIncomingTimestamp)+ ")"    );
            throw new UngracefulFailureException("Silent");//Stop node
        }
        if(job.numberOfByzantianFailures == 1) {
            log.info("Byzantian failure at "+workerId+" at "+System.currentTimeMillis());
            job.setResult((Integer)(int)(Math.random()*1000));//Random value
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
        for(ActorSelection node:headnodes) {
            node.tell(new ConfirmRegistrationResult(workerId), this.self());
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

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
        if(!this.silentFailing) {
            sendRemove();
        }
        log.info("Worker " +workerId+" "+"stop failing at "+System.currentTimeMillis());
        super.preRestart(reason, message);
    }

    void terminateSystem(TerminateSystem message) {
        log.info("//////////WORKER-DONE");
        System.exit(0);
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
                .match(
                        TerminateSystem.class, this::terminateSystem
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
     * Message send WorkerNode to confirm it can receive responses
     */
    public static class ConfirmRegistrationResult implements Serializable {
        Integer workerId;

        public ConfirmRegistrationResult(Integer id) {
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

    /**
     * Message send from HeadNode to terminate the system
     */
    public static class TerminateSystem implements Serializable {

    }



}