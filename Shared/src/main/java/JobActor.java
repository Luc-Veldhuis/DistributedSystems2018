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
        log.info("Received job "+message.jobHandler.debugId+" with id "+ message.jobHandler.getId()+ " at " + System.currentTimeMillis() );
        this.doneHander.accept(message.jobHandler.getResult());
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
