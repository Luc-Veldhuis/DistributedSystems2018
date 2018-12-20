import akka.actor.ActorRef;
import akka.event.LoggingAdapter;

public class Scheduler{

    public Policy policy;
    /**
     * Wrapper class for the policy, choise of policy should be made here.
     */
    public Scheduler(HeadNodeState state, ActorRef headNode, Configuration config, LoggingAdapter log) {
        //Choose which policy to used based on configuration
        if(config.policy == Configuration.Policies.LOCK_STEP) {
            this.policy = new LockStepPolicy(state, headNode, config, log);
            log.info("Using lock step policy");
        }
        else if(config.policy == Configuration.Policies.MAXIMIZE) {
            this.policy = new MaximizePolicy(state, headNode, config,log);
            log.info("Using maximize policy");
        }else if (config.policy == Configuration.Policies.SAME_MACHINE) {
            this.policy = new SameMachinePolicy(state, headNode, config, log);
            log.info("Using Same machine policy");
        } else {
            throw new Error("Unknown policy in configuration file");
        }
    }

    public void update(JobHandler jobHandler, ActorRef jobActor) {
        this.policy.update(jobHandler, jobActor);
    }

    public JobWaiting update(JobHandler jobHandler, WorkerData worker) {
        return this.policy.update(jobHandler, worker);
    }

    public void removeWorker(Integer workerId) {
        this.policy.removeWorker(workerId);
    }

    public void dispatchJob() {
        this.policy.dispatchJob();
    }
}
