import java.util.ArrayList;
import java.util.List;

public class JobWaiting {

    public JobHandler jobHander;//main job
    List<JobHandler> jobList;
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

    public boolean isDone() {
        boolean done = true;
        for(JobHandler job: jobList) {
            done = done && job.done;
        }
        return done;
    }

    public void newResult(JobHandler job) {
        for(JobHandler storedJob: jobList) {
            if(job.getId().equals(storedJob.getId())) {
                //TODO check this copy, probabily does not work for objects?
                storedJob.result = job.result;
                storedJob.e = job.e;
                counter++;
                break;
            }
        }
    }


}
