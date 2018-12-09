import akka.actor.ActorRef;

/**
 * Lock step policy, releases worker only after all corresponding jobs are done
 */
public class MaximizePolicy implements PolicyInterface {

    //Number every JobHandler
    int idCounter = 0;
    ActorRef headNode;
    HeadNodeState state;

    MaximizePolicy(HeadNodeState state, ActorRef headNode) {
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
        state.jobsWaitingForExecutionResults.put(jobHandler.getId(), jobWaiting);
        for (int i = 0; i < Configuration.NUMBER_OF_BYZANTIAN_ERRORS; i++) {
            //Clone original job into x copies
            JobHandler newJob = jobWaiting.jobHander.clone();
            newJob.setId(jobWaiting.jobHander.getId() + "-" + i);
            newJob.setParentId(jobWaiting.jobHander.getId());

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
            state.jobHandlerForExecution.remove(jobHandlerId);
            //And corresponding jobWaiting
            JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobHandler.getParentId());

            //Get the first passive worker
            Integer node = state.passiveWorkers.get(0);
            state.passiveWorkers.remove(node);//remove first node
            state.activeWorkers.add(node); // add it to active

            ActorRef workerNodeRef = state.workerIdToWorkerNode.get(node);//Get actor reference
            jobWaiting.jobList.add(new Pair<JobHandler, Integer>(jobHandler, node));//Add to waiting job
            workerNodeRef.tell(new WorkerNode.GetJobFromHead(jobHandler), headNode);//Run job

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
        }
        //Only after added workers to active, call dispatcher
        dispatchJob();//Get first job in FIFO manner
        return jobWaiting;
    }
}
