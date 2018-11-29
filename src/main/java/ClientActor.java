import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.lang.reflect.Method;

public class ClientActor<E> extends AbstractActor {

    ActorRef headNodeRef;
    Messages messages;
    Method doneHander;

    public static Props props(ActorRef headNodeRef, JobHandler job, Method doneHander) {
        System.out.println("Client job created");
        return Props.create(ClientActor.class, () -> new ClientActor(headNodeRef, job, doneHander));
    }

    public ClientActor(ActorRef headNodeRef, JobHandler job, Method doneHander) {
        this.headNodeRef = headNodeRef;
        this.doneHander = doneHander;
        this.messages = new Messages();
        headNodeRef.tell(this.messages.getJobFromClient(job, this), this.self());
    }

    public void receivedJob(Messages.GetJobFromHead message) throws Exception {
        this.doneHander.invoke(message.job.getResult());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                    Messages.GetJobFromHead.class, this::receivedJob
                ).build();
    }
}
