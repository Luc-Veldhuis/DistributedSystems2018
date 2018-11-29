import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Job<E> implements JobInterface<E> {

    private static int counter = 0;

    private JobHandler<E> jobHandler;
    private Consumer doneHandler;
    ActorSystem root = ActorSystem.create("root-node");
    private ActorRef headNodeRef;

    public Job(ActorRef headNodeRef) {
        this.headNodeRef = headNodeRef;
    }

    public Job(ActorRef headNodeRef, Supplier job) {
        this.headNodeRef = headNodeRef;
        this.jobHandler = new JobHandler<E>(job);
    }

    public Job(ActorRef headNodeRef, Supplier job, Consumer hander) {
        this.headNodeRef = headNodeRef;
        this.jobHandler = new JobHandler<E>(job);
        this.doneHandler = hander;
    }

    public void setJob(Supplier job) {
        this.jobHandler = new JobHandler<E>(job);
    }

    public void setHandler(Consumer handler) {
        this.doneHandler = handler;
    }

    @Override
    public void run() throws Exception {
        if(this.doneHandler == null || this.jobHandler == null) {
            throw new Exception("Not all handers set");
        }
        ActorRef clientActor = this.root.actorOf(ClientActor.props(headNodeRef, this.jobHandler, this.doneHandler), "client-job-" + (counter++));
    }
}
