import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;

import java.io.Serializable;
import java.util.function.Consumer;

public class JobActor<E> extends AbstractActor {

    ActorSelection headNodeRef;
    Consumer doneHander;

    public static Props props(ActorSelection headNodeRef, JobHandler job, Consumer doneHander) {
        System.out.println("Client job created");
        return Props.create(JobActor.class, () -> new JobActor(headNodeRef, job, doneHander));
    }

    public JobActor(ActorSelection headNodeRef, JobHandler job, Consumer doneHander) {
        this.headNodeRef = headNodeRef;
        this.doneHander = doneHander;
        headNodeRef.tell(new GetJobFromClient(job), this.self());
    }

    public void receivedJob(GetJobFromHead message) throws Exception {
        //this.doneHander.run(message.job.getResult());
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

    public static class GetJobFromClient implements Serializable {
        public JobHandler jobHandler;

        public GetJobFromClient(JobHandler jobHandler) {
            this.jobHandler = jobHandler;
        }
    }

    public static class GetJobFromHead implements Serializable {
        public JobHandler jobHandler;

        public GetJobFromHead(JobHandler jobHandler) {
            this.jobHandler = jobHandler;
        }
    }
}
