import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Job<E> implements JobInterface<E> {

    private static int counter = 0;

    private JobHandler<E> jobHandler;
    private Consumer doneHandler;
    ActorSystem root = ActorSystem.create("root-node");
    private ActorSelection headNodeRef;

    public Job(String headNode) {
        this.headNodeRef = root.actorSelection(headNode);
    }

    public Job(ActorSelection headNode, Supplier job) {
        this.headNodeRef = headNode;
        this.jobHandler = new JobHandler<E>(job);
    }

    public Job(ActorSelection headNode, Supplier job, Consumer hander) {
        this.headNodeRef = headNode;
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
        ActorRef clientActor = this.root.actorOf(JobActor.props(headNodeRef, this.jobHandler, this.doneHandler), "client-job-" + (counter++));
    }
}
