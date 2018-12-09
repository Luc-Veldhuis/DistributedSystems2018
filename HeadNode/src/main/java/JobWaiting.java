import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JobWaiting implements Serializable {

    public JobHandler jobHander;//main job
    List<Pair<JobHandler, Integer>> jobList; //Store the job and the node on which it is run worker
    int counter = 0;

    /**
     * Once a Job has been copied in x separate Jobs, this class stores the state of all of the jobs
     * @param jobHandler
     */
    JobWaiting(JobHandler jobHandler) {
        //TODO Update on failing node, restart job, otherwise it waits forever on a job which is not restarted
        this.jobHander = jobHandler;
        jobList = new ArrayList<>();
    }

    /**
     * Checks if all x copies are done
     * @return
     */
    public boolean isDone() {
        if(jobList.size() != Configuration.NUMBER_OF_BYZANTIAN_ERRORS) {
            return false;
        }
        boolean done = true;
        for(Pair<JobHandler, Integer> job: jobList) {
            done = done && job.first.done;
        }
        return done;
    }

    /**
     * If a copy is done, call this function
     * @param job
     */
    public void newResult(JobHandler job) {
        for(Pair<JobHandler, Integer> storedJob: jobList) {
            if(job.getId().equals(storedJob.first.getId())) {
                storedJob.first.setResult(job.result);
                storedJob.first.setException(job.e);
                counter++;
                break;
            }
        }
    }

}
