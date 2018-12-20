import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JobWaiting implements Serializable {

    public JobHandler jobHander;//main job
    List<Pair<JobHandler, Integer>> jobList; //Store the job and the node on which it is run worker
    int counter = 0;

    LoggingAdapter log;


    /**
     * Once a Job has been copied in x separate Jobs, this class stores the state of all of the jobs
     * @param jobHandler
     */
    JobWaiting(JobHandler jobHandler, LoggingAdapter log) {
        //TODO Update on failing node, restart job, otherwise it waits forever on a job which is not restarted
        this.jobHander = jobHandler;
        this.log = log;
        jobList = new ArrayList<>();
    }

    /**
     * Checks if all x copies are done
     * @return
     */
    public boolean isDone(int expectedSize) {
        if(jobList.size() != expectedSize) {
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
          //job.debugId

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
