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
            //it is executing a job
            //execute this jobs again, because it is maximize
            for(String jobWaitingId : state.jobsWaitingForExecutionResults.keySet()) {
                JobHandler jobHanderFailing = null;
                for( Pair<JobHandler, Integer> pair : state.jobsWaitingForExecutionResults.get(jobWaitingId).jobList) {
                    if(pair.second.equals(workerId)) {
                        //Found jobHandler which failed
                        jobHanderFailing = pair.first;
                    }
                }
                if(jobHanderFailing != null) {
                    //run job again
                    JobHandler newJob = jobHanderFailing.clone();
                    state.jobHanderQueue.add(newJob.getId());
                    state.jobHandlerForExecution.put(newJob.getId(), newJob);
                    dispatchJob();
                    break;
                }
            }
            state.activeWorkers.remove(workerId);//remove from active workers
        }
        state.workerIdToWorkerNode.remove(workerId);//remove from workerId mapping
    }
}
