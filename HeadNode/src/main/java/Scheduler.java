import akka.actor.ActorRef;

public class Scheduler implements PolicyInterface {

    public PolicyInterface policy;
    /**
     * Wrapper class for the policy, choise of policy should be made here.
     */
    public Scheduler(HeadNodeState state, ActorRef headNode) {
        //Choose which policy to used based on configuration
        if(Configuration.policy == Configuration.Policies.LOCK_STEP) {
            this.policy = new LockStepPolicy(state, headNode);
            System.out.println("Using lock step policy");
        }
        else if(Configuration.policy == Configuration.Policies.MAXIMIZE) {
            this.policy = new MaximizePolicy(state, headNode);
            System.out.println("Using maximize policy");
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
