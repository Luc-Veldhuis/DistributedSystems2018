import akka.actor.ActorRef;

public abstract class Policy {

    /**
     * Make sure that we can use different  Policys and reference this interface as Type
     * @param jobHandler
     * @param jobActor
     */
    //We have to think about what functions a policy really needs

    //update when job comes in
    public abstract void update(JobHandler jobHandler, ActorRef jobActor);

    //update when job finishes
    public abstract JobWaiting update(JobHandler jobHandler, WorkerData worker);

    //
    public abstract void removeWorker(Integer workerId);

    public abstract void dispatchJob();

    public void addRandomFailures(JobHandler jobHander, Configuration config) {
        if(config.RANDOM_FAILURES) {
            for(int i = 0 ; i < config.RATE_OF_BYZANTINE_FAILURES; i++) {
                if (Math.random() < config.RATE_OF_BYZANTINE_FAILURES) {
                    jobHander.numberOfByzantianFailures++;
                }
                if (Math.random() < config.RATE_OF_SILENT_FAILURES) {
                    jobHander.numberOfFailSilentFailures++;
                }
                if (Math.random() < config.RATE_OF_STOP_FAILURES) {
                    jobHander.numberOfFailStopFailures++;
                }
            }
        }
        if(jobHander.numberOfByzantianFailures > Configuration.NUMBER_OF_ERRORS_TO_CORRECT) {
            jobHander.numberOfByzantianFailures = Configuration.NUMBER_OF_ERRORS_TO_CORRECT;
        }
    }

    public void addFailures(JobHandler newJob, JobHandler jobHander) {
        if(jobHander.numberOfByzantianFailures > 0) {
            newJob.numberOfByzantianFailures = 1;
            jobHander.numberOfByzantianFailures--;
        }
        if(jobHander.numberOfFailSilentFailures > 0) {
            newJob.numberOfFailSilentFailures = 1;
            jobHander.numberOfFailSilentFailures--;
        }
        if(jobHander.numberOfFailStopFailures > 0) {
            newJob.numberOfFailStopFailures = 1;
            jobHander.numberOfFailStopFailures--;
        }
    }

}
