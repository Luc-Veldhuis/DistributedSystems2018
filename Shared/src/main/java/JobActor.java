import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.Serializable;
import java.util.function.Consumer;

public class JobActor<E> extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    ActorSelection[] headNodeRefs;
    Consumer doneHander;

    static int number_of_jobs_to_be_yet_finished = Configuration.NUMBER_OF_JOBS;
    static boolean decrementJobs() {
        synchronized(JobActor.class) {
            return number_of_jobs_to_be_yet_finished-- < 0;
        }
    }

    /**
     * Actor used to communicate with head node, spawed by the Job class
     * @param headNodeRef Location where headNode is located
     * @param job jobHandler to execute
     * @param doneHander Function to execute when done
     * @return new Actor
     */
    public static Props props(ActorSelection[] headNodeRef, JobHandler job, Consumer doneHander) {
        System.out.println("Client job created");
        return Props.create(JobActor.class, () -> new JobActor(headNodeRef, job, doneHander));
    }

    public JobActor(ActorSelection[] headNodeRefs, JobHandler job, Consumer doneHander) {
        this.headNodeRefs = headNodeRefs;
        this.doneHander = doneHander;
        log.info("Job "+job.debugId+" created at client at " + System.currentTimeMillis() );
        job.originalCreationTime = System.currentTimeMillis();


        for(ActorSelection headNodeRef: headNodeRefs) {
            headNodeRef.tell(new GetJobFromClient(job), this.self());
        }
    }

    /**
     * Function which is called once the job is done
     * @param message result from HeadNode
     * @throws Exception possible error from the Job
     */
    public void receivedJob(GetJobFromHead message) throws Exception {
        long receivedTime = System.currentTimeMillis();

        log.info("Received job "+message.jobHandler.debugId+" with id "+ message.jobHandler.getId()+ " at " + receivedTime + " with result " + message.jobHandler.getResult() );
        //Format: ////CLIENT-FINISHED-JOB: (originalCreationTime, receivedTime, latency (received-orig), expected duration, finished timestamp, len of parentID,parentId)
        //log.info("////CLIENT-FINISHED-JOB: ("+message.jobHandler.originalCreationTime+","+receivedTime+","+ (receivedTime - message.jobHandler.originalCreationTime) +"," + message.jobHandler.input +","+ message.jobHandler.finishedTime +"," +message.jobHandler.parentId.length()+","+message.jobHandler.parentId+")" );
        log.info("//CLIENT-FINISHED-JOB: ("+message.jobHandler.getParentId()+","+ (System.currentTimeMillis() - message.jobHandler.originalCreationTime) +","+ message.jobHandler.input  + ")"    );

        this.doneHander.accept(message.jobHandler.getResult());

        Utils.runNextJob();

        boolean doneTestingWorkload = decrementJobs();
        if(doneTestingWorkload) {
            log.info("/////CLIENT-DONE");
            System.exit(0);
        }

        getContext().stop(self());//Prevent having a lot of never used again actors
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                    GetJobFromHead.class, this::receivedJob
                ).build();
    }

    /**
     * Message to communicate job to HeadNode
     */
    public static class GetJobFromClient implements Serializable {
        public JobHandler jobHandler;

        public GetJobFromClient(JobHandler jobHandler) {
            this.jobHandler = jobHandler;
        }
    }

    /**
     * Message to communicate response from HeadNode
     */
    public static class GetJobFromHead implements Serializable {
        public JobHandler jobHandler;

        public GetJobFromHead(JobHandler jobHandler) {
            this.jobHandler = jobHandler;
        }
    }
}
