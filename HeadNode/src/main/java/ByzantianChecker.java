import akka.actor.ActorRef;

public class ByzantianChecker {

    public HeadNodeState state;

    /**
     * Used to check if any Job contains Byzantine errors and to correct these
     * @param state the HeadNode state
     */
    public ByzantianChecker(HeadNodeState state) {
        this.state = state;
    }

    /**
     * Called when a Job is done
     * @param job the JobWaiting to check
     * @return JobHandler to return to client
     */
    public JobHandler check(JobWaiting job) {
        JobHandler result = job.jobHander;
        for( Pair<JobHandler, ActorRef> pair: job.jobList) {
            int counter = 0;
            JobHandler jobHandler = pair.first;
            for( Pair<JobHandler, ActorRef> pair2: job.jobList) {
                JobHandler jobHandler2 = pair2.first;
                if(jobHandler.equals(jobHandler2)) {
                    counter++;
                }
            }
            if(counter >= Math.ceil(Configuration.NUMBER_OF_BYZANTIAN_ERRORS/2.)) {//make sure to check k+1 errors
                //found enough equal results for this result to know it is not byzantian
                result.setResult(jobHandler.result);
                result.setException(jobHandler.e);
                break;
            }
        }
        return result;
    }
}
