import akka.actor.ActorRef;
import akka.event.LoggingAdapter;

public class Scheduler implements PolicyInterface {

    public PolicyInterface policy;
    /**
     * Wrapper class for the policy, choise of policy should be made here.
     */
    public Scheduler(HeadNodeState state, ActorRef headNode, LoggingAdapter log) {
        //Choose which policy to used based on configuration
        if(Configuration.policy == Configuration.Policies.LOCK_STEP) {
            this.policy = new LockStepPolicy(state, headNode, log);
            log.info("Using lock step policy");
        }
        else if(Configuration.policy == Configuration.Policies.MAXIMIZE) {
            this.policy = new MaximizePolicy(state, headNode, log);
            log.info("Using maximize policy");
        }else if (Configuration.policy == Configuration.Policies.SAME_MACHINE) {
            this.policy = new SameMachinePolicy(state, headNode, log);
            log.info("Using Same machine policy");
        } else {
            throw new Error("Unknown policy in configuration file");
        }
    }

    @Override
    public void update(JobHandler jobHandler, ActorRef jobActor) {
        this.policy.update(jobHandler, jobActor);
    }

    @Override
    public JobWaiting update(JobHandler jobHandler, WorkerData worker) {
        return this.policy.update(jobHandler, worker);
    }

    @Override
    public void removeWorker(Integer workerId) {
        this.policy.removeWorker(workerId);
    }

    @Override
    public void dispatchJob() {
        this.policy.dispatchJob();
    }
}
