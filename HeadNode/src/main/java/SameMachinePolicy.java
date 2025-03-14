import akka.actor.ActorRef;
import akka.event.LoggingAdapter;

/**
 * Lock step policy, releases worker only after all corresponding jobs are done
 */
public class SameMachinePolicy extends Policy {

    //Number every JobHandler
    int idCounter = 0;
    ActorRef headNode;
    HeadNodeState state;
    LoggingAdapter log;
    Configuration config;

    SameMachinePolicy(HeadNodeState state, ActorRef headNode, Configuration config, LoggingAdapter log) {
        if(state == null || headNode == null) {
            throw new InstantiationError();
        }
        this.state = state;
        this.headNode = headNode;
        this.log = log;
        this.config = config;
    }
    /**
     * Used to update the schedule when a client job comes in
     * @param jobHandler
     * @param jobActor
     */
    @Override
    public void update(JobHandler jobHandler, ActorRef jobActor) {
        //added
        addRandomFailures(jobHandler, config);
        jobHandler.setId(idCounter+"");
        //log.info("Assigned job "+jobHandler.debugId + " interal id "+jobHandler.getId());
        log.info("Job "+ jobHandler.getId() + " has errors: "+ jobHandler.numberOfByzantianFailures + " "+ jobHandler.numberOfFailStopFailures + " "+ jobHandler.numberOfFailSilentFailures);
        JobWaiting jobWaiting = new JobWaiting(jobHandler, log);
        state.jobClientMapping.put(jobWaiting.jobHander.getId(), jobActor);
        state.jobsWaitingForExecutionResults.put(jobHandler.getId(), jobWaiting);
        state.jobWaitingQueue.add(jobHandler.getId());
        dispatchJob();
        idCounter++;

    }

    public void dispatchSingleJob(JobWaiting jobWaiting, Integer node) {
        //Clone
        JobHandler newJob = jobWaiting.jobHander.clone();
        newJob.setId(jobWaiting.jobHander.getId() + "-" + (jobWaiting.jobList.size()));//Add first element to the list
        newJob.setParentId(jobWaiting.jobHander.getId());
        addFailures(newJob, jobWaiting.jobHander);

        ActorRef workerNodeRef = state.workerIdToWorkerNode.get(node);//Get actor reference
        jobWaiting.jobList.add(new Pair<JobHandler, Integer>(newJob, node));//Add to waiting job
        workerNodeRef.tell(new WorkerNode.GetJobFromHead(newJob), headNode);//Run job
        log.info("Send job "+newJob.getId()+" to worker node "+ node);
    }

    /**
     * Used to send a job to a WorkerNode
     */
    public void dispatchJob() {
        //Dispatch as long as possible
        while(state.passiveWorkers.size() >= 1) {
            //Get jobHanderId to execute
            String jobWaitingId = state.jobWaitingQueue.poll();//No longer waiting for execution
            if (jobWaitingId == null) {
                //Queue empty
                return;
            }
            //Get JobWaiting and remove from mapping
            JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobWaitingId);

            //Get the first passive worker
            Integer node = state.passiveWorkers.get(0);
            state.passiveWorkers.remove(node);//remove first node
            state.activeWorkers.add(node); // add it to active

            dispatchSingleJob(jobWaiting, node);
        }

    }

    /**
     * Called when a WorkerNode is finished
     * @param jobHandler
     * @param workerNode
     */
    @Override
    public JobWaiting update(JobHandler jobHandler, WorkerData workerNode){
        //done
        JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobHandler.getParentId());//Get waiting job
        jobWaiting.newResult(jobHandler);
        if(jobWaiting.isDone(config.NUMBER_OF_DUPLICATIONS)) {
            state.jobsWaitingForExecutionResults.remove(jobWaiting.jobHander.getId());
            for(Pair<JobHandler, Integer> pair : jobWaiting.jobList) {
                state.jobHandlerForExecution.remove(pair.first.getId());//remove later in case a worker crashes
            }
            state.activeWorkers.remove(workerNode.workerId);//worker is done
            state.passiveWorkers.add(workerNode.workerId);//worker is passive
        } else {
            dispatchSingleJob(jobWaiting, workerNode.workerId);
        }
        dispatchJob();
        return jobWaiting;
    }

    /**
     * Called when a worker is removed
     * @param workerId Worker to be removed
     */
    public void removeWorker(Integer workerId) {
        if(!state.passiveWorkers.remove(workerId)) {
            state.activeWorkers.remove(workerId);//remove from active workers
            //it is executing a job
            //execute this jobs again, because it is maximize
            log.info("Failing worker "+workerId+ " is active");
            for(String jobWaitingId : state.jobsWaitingForExecutionResults.keySet()) {
                boolean found = false;
                JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobWaitingId);
                for( Pair<JobHandler, Integer> pair : jobWaiting.jobList) {
                    if(pair.second.equals(workerId) && !pair.first.done) {
                        //Found jobHandler which failed
                        found = true;
                    }
                }
                if(found) {
                    //Delete all job data
                    ActorRef client = state.jobClientMapping.get(jobWaiting.jobHander.getId());
                    state.jobClientMapping.remove(jobWaiting.jobHander.getId());
                    state.jobsWaitingForExecutionResults.remove(jobWaitingId);
                    //restart job
                    log.info("Running job "+jobWaiting.jobHander.getId()+" again");
                    update(jobWaiting.jobHander,client);
                    break;
                }
            }
        }
        state.workerIdToWorkerNode.remove(workerId);//remove from workerId mapping
    }
}
