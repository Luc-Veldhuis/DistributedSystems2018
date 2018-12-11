import akka.actor.ActorRef;
import akka.event.LoggingAdapter;

/**
 * Lock step policy, releases worker only after all corresponding jobs are done
 */
public class MaximizePolicy extends Policy {

    //Number every JobHandler
    int idCounter = 0;
    ActorRef headNode;
    HeadNodeState state;
    LoggingAdapter log;

    MaximizePolicy(HeadNodeState state, ActorRef headNode, LoggingAdapter log) {
        if(state == null || headNode == null) {
            throw new InstantiationError();
        }
        this.state = state;
        this.headNode = headNode;
        this.log = log;
    }
    /**
     * Used to update the schedule when a client job comes in
     * @param jobHandler
     * @param jobActor
     */
    @Override
    public void update(JobHandler jobHandler, ActorRef jobActor) {
        //added
        addRandomFailures(jobHandler);
        jobHandler.setId(idCounter+"");
        log.info("Job "+ jobHandler.getId() + " has errors: "+ jobHandler.numberOfByzantianFailures + " "+ jobHandler.numberOfFailStopFailures + " "+ jobHandler.numberOfFailSilentFailures);
        JobWaiting jobWaiting = new JobWaiting(jobHandler);
        state.jobClientMapping.put(jobWaiting.jobHander.getId(), jobActor);
        state.jobsWaitingForExecutionResults.put(jobHandler.getId(), jobWaiting);
        for (int i = 0; i < Configuration.NUMBER_OF_DUPLICATIONS; i++) {
            //Clone original job into x copies
            JobHandler newJob = jobWaiting.jobHander.clone();
            newJob.setId(jobWaiting.jobHander.getId() + "-" + i);
            newJob.setParentId(jobWaiting.jobHander.getId());
            addFailures(newJob, jobWaiting.jobHander);
            //add to the queue
            state.jobHanderQueue.add(newJob.getId());
            state.jobHandlerForExecution.put(newJob.getId(), newJob);
        }

        dispatchJob();
        idCounter++;

    }

    /**
     * Used to send a job to a WorkerNode
     */
    public void dispatchJob() {
        //Dispatch as long as possible
        while(state.passiveWorkers.size() >= 1) {
            //Get jobHanderId to execute
            String jobHandlerId = state.jobHanderQueue.poll();//No longer waiting for execution
            if (jobHandlerId == null) {
                //Queue empty
                return;
            }
            //Get JobHander and remove from mapping
            JobHandler jobHandler = state.jobHandlerForExecution.get(jobHandlerId);
            //And corresponding jobWaiting
            JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobHandler.getParentId());

            //Get the first passive worker
            Integer node = state.passiveWorkers.get(0);
            state.passiveWorkers.remove(node);//remove first node
            state.activeWorkers.add(node); // add it to active

            ActorRef workerNodeRef = state.workerIdToWorkerNode.get(node);//Get actor reference
            jobWaiting.jobList.add(new Pair<JobHandler, Integer>(jobHandler, node));//Add to waiting job
            workerNodeRef.tell(new WorkerNode.GetJobFromHead(jobHandler), headNode);//Run job
            log.info("Headnode send job "+jobHandler.getId()+" to worker node "+ node);
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
        state.activeWorkers.remove(workerNode.workerId);//worker is done
        state.passiveWorkers.add(workerNode.workerId);//worker is passive
        if(jobWaiting.isDone()) {
            state.jobsWaitingForExecutionResults.remove(jobWaiting.jobHander.getId());
            for(Pair<JobHandler, Integer> pair : jobWaiting.jobList) {
                state.jobHandlerForExecution.remove(pair.first.getId());//remove later in case a worker crashes
            }
        }
        //Only after added workers to active, call dispatcher
        dispatchJob();//Get first job in FIFO manner
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
                Pair<JobHandler, Integer> pairFailing = null;
                JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobWaitingId);
                for( Pair<JobHandler, Integer> pair : jobWaiting.jobList) {
                    if(pair.second.equals(workerId) && !pair.first.done) {
                        //Found jobHandler which failed
                        pairFailing = pair;
                    }
                }
                if(pairFailing != null) {
                    //run job again
                    jobWaiting.jobList.remove(pairFailing);
                    JobHandler newJob = pairFailing.first.clone();
                    log.info("Running job "+newJob.getId()+" again");
                    state.jobHanderQueue.add(newJob.getId());
                    state.jobHandlerForExecution.put(newJob.getId(), newJob);
                    dispatchJob();
                    break;
                }
            }
        }
        state.workerIdToWorkerNode.remove(workerId);//remove from workerId mapping
    }
}
