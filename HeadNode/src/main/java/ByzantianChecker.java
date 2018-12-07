public class ByzantianChecker {

    public HeadNodeState state;

    /**
     * Used to check if any Job contains Byzantine errors and to correct these
     * @param state
     */
    public ByzantianChecker(HeadNodeState state) {
        this.state = state;
    }

    /**
     * Called when a Job is done
     * @param job
     * @return
     */
    public boolean check(JobHandler job) {
        //TODO check for byzantian errors here, otherwise restart the job
        return true;
    }
}
