import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

import java.util.function.Consumer;

public class Job<E> implements JobInterface<E> {
    /**
     * Class with which you can interact to create a new Job
     *
     */

    private static int counter = 0;
    private static ActorSystem root = ActorSystem.create("root-node");

    private JobHandler<E> jobHandler;
    private Consumer doneHandler;
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
     * @param job Job to execute on worker
     */
    public void setJob(SerializableSupplier job) {
        this.jobHandler = new JobHandler<E>(job);
    }

    /**
     * Called once the function is done
     * @param handler Function to execute on termination
     */
    public void setHandler(SerializableConsumer handler) {
        this.doneHandler = handler;
    }

    /**
     * Used for debugging
     * @param numberOfByzantianFailures Set the number of Byzantine failures
     * @param numberOfFailSilentFailures Set the number of Fail-Silent failures
     * @param numberOfFailStopFailures Set the number of Fail-Stop failures
     */
    public void setErrors(int numberOfByzantianFailures, int numberOfFailSilentFailures, int numberOfFailStopFailures) {
        this.jobHandler.numberOfByzantianFailures = numberOfByzantianFailures;
        this.jobHandler.numberOfFailSilentFailures = numberOfFailSilentFailures;
        this.jobHandler.numberOfFailStopFailures = numberOfFailStopFailures;
    }

    public void setHeadNodeCrash(int crashHeadNodeWithId) {
        this.jobHandler.crashHeadNodeWithId = crashHeadNodeWithId;
    }

    /**
     * Execute the function
     * @throws Exception Exception thrown when job resulted in error
     */
    @Override
    public void run() throws Exception {
        if(this.doneHandler == null || this.jobHandler == null) {
            throw new Exception("Not all handers set");
        }
        ActorRef jobActor = this.root.actorOf(JobActor.props(headNodeRef, this.jobHandler, this.doneHandler), "client-job-" + (counter++));
    }
}
