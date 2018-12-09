import akka.actor.ActorRef;

/**
 * Lock step policy, releases worker only after all corresponding jobs are done
 */
public class LockStepPolicy implements PolicyInterface {

    //Number every JobHandler
    int idCounter = 0;
    ActorRef headNode;
    HeadNodeState state;

    LockStepPolicy(HeadNodeState state, ActorRef headNode) {
        if(state == null || headNode == null) {
            throw new InstantiationError();
        }
        this.state = state;
        this.headNode = headNode;
    }
    /**
     * Used to update the schedule when a client job comes in
     * @param jobHandler
     * @param jobActor
     */
    @Override
    public void update(JobHandler jobHandler, ActorRef jobActor) {
        //added
        jobHandler.setId(idCounter+"");
        JobWaiting jobWaiting = new JobWaiting(jobHandler);
        state.jobClientMapping.put(jobWaiting.jobHander.getId(), jobActor);
        state.jobsWaitingForExecution.put(jobWaiting.jobHander.getId(),jobWaiting);
        dispatchJob(jobWaiting);
        idCounter++;

    }

    /**
     * Used to send a job to a WorkerNode
     * @param jobWaiting
     */
    public void dispatchJob(JobWaiting jobWaiting) {
        //Only dispatch in lock step, all at the same time
        if(!(state.passiveWorkers.size() >= Configuration.NUMBER_OF_BYZANTIAN_ERRORS)) {
            return;//Do not dispatch jobs
        }
        //Spawn x jobsWaitingForExecution
        state.jobsWaitingForExecution.remove(jobWaiting.jobHander.getId());//No longer waiting for execution
        state.jobsWaitingForExecutionResults.put(jobWaiting.jobHander.getId(), jobWaiting); //Waiting for execution to finish
        for (int i = 0; i < Configuration.NUMBER_OF_BYZANTIAN_ERRORS; i++) {
            //Clone original job into x copies
            JobHandler newJob = jobWaiting.jobHander.clone();
            newJob.setId(jobWaiting.jobHander.getId() + "-" + i);
            newJob.setParentId(jobWaiting.jobHander.getId());
            //Get the first passive worker
            Integer node = state.passiveWorkers.get(0);
            state.passiveWorkers.remove(node);//remove first node
            state.activeWorkers.add(node); // add it to active

            ActorRef workerNodeRef = state.workerIdToWorkerNode.get(node);//Get actor reference
            jobWaiting.jobList.add(new Pair<JobHandler, ActorRef>(newJob, workerNodeRef));//Add to waiting job
            state.jobsRunning.add(newJob.getId());
            workerNodeRef.tell(new WorkerNode.GetJobFromHead(newJob), headNode);//Run job
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
        state.jobsRunning.remove(jobHandler.getId());//Job is done
        state.activeWorkers.remove((Integer)workerNode.workerId);//worker is done
        state.passiveWorkers.add(workerNode.workerId);//worker is passive
        JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobHandler.getParentId());//Get waiting job
        jobWaiting.newResult(jobHandler);
        if(!state.jobsWaitingForExecution.isEmpty()) {
            dispatchJob(state.jobsWaitingForExecution.get(state.jobsWaitingForExecution.keySet().toArray()[0]));//Get first job waiting in FIFO manner
        }
        return jobWaiting;
    }
}
