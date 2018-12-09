import akka.actor.ActorRef;

import java.util.ArrayList;
import java.util.List;

public class JobWaiting {

    public JobHandler jobHander;//main job
    List<Pair<JobHandler, ActorRef>> jobList; //Store the job and the node on which it is run worker
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
        boolean done = true;
        for(Pair<JobHandler, ActorRef> job: jobList) {
            done = done && job.first.done;
        }
        return done;
    }

    /**
     * If a copy is done, call this function
     * @param job
     */
    public void newResult(JobHandler job) {
        for(Pair<JobHandler, ActorRef> storedJob: jobList) {
            if(job.getId().equals(storedJob.first.getId())) {
                storedJob.first.setResult(job.result);
                storedJob.first.setException(job.e);
                counter++;
                break;
            }
        }
    }

}
