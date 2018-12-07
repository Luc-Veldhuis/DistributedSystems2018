import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Job<E> implements JobInterface<E> {
    /**
     * Class with which you can interact to create a new Job
     *
     */

    private static int counter = 0;

    private JobHandler<E> jobHandler;
    private Consumer doneHandler;
    ActorSystem root = ActorSystem.create("root-node");
    private ActorSelection headNodeRef;

    public Job(String headNode) {
        this.headNodeRef = root.actorSelection(headNode);
    }

    public Job(ActorSelection headNode, SerializableSupplier job) {
        this.headNodeRef = headNode;
        this.jobHandler = new JobHandler<E>(job);
    }

    public Job(ActorSelection headNode, SerializableSupplier job, Consumer hander) {
        this.headNodeRef = headNode;
        this.jobHandler = new JobHandler<E>(job);
        this.doneHandler = hander;
    }

    /**
     * Called to set the function to run on the worker
     * @param job
     */
    public void setJob(SerializableSupplier job) {
        this.jobHandler = new JobHandler<E>(job);
    }

    /**
     * Called once the function is done
     * @param handler
     */
    public void setHandler(SerializableConsumer handler) {
        this.doneHandler = handler;
    }

    /**
     * Execute the function
     * @throws Exception
     */
    @Override
    public void run() throws Exception {
        if(this.doneHandler == null || this.jobHandler == null) {
            throw new Exception("Not all handers set");
        }
        ActorRef jobActor = this.root.actorOf(JobActor.props(headNodeRef, this.jobHandler, this.doneHandler), "client-job-" + (counter++));
    }
}
