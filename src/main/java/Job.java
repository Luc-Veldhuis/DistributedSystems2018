import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Job<E> implements JobInterface<E> {

    private static int counter = 0;

    private JobHandler<E> jobHandler;
    private Consumer doneHandler;
    ActorSystem root = ActorSystem.create("root-node");
    private ActorSelection headNodeRef;

    public Job(String headNodeUri) {
        this.headNodeRef = this.root.actorSelection(headNodeUri);//TODO create actorRef from url
    }

    public Job(String headNodeUri, Supplier job) {
        this.headNodeRef = this.root.actorSelection(headNodeUri);
        this.jobHandler = new JobHandler<E>(job);
    }

    public Job(String headNodeUri, Supplier job, Consumer hander) {
        this.headNodeRef = this.root.actorSelection(headNodeUri);
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
