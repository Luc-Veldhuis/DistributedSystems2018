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
        state.jobsWaitingForExecutionResults.put(jobWaiting.jobHander.getId(),jobWaiting);
        state.jobWaitingQueue.add(jobWaiting.jobHander.getId());
        dispatchJob();
        idCounter++;

    }

    /**
     * Used to send a job to a WorkerNode
     */
    public void dispatchJob() {
        //Only dispatch in lock step, all at the same time
        if(!(state.passiveWorkers.size() >= Configuration.NUMBER_OF_BYZANTIAN_ERRORS)) {
            return;//Do not dispatch jobs
        }
        //Spawn x jobsWaitingForExecution
        String jobWaitingId = state.jobWaitingQueue.poll();//No longer waiting for execution
        if(jobWaitingId == null) {
            //Queue empty
            return;
        }
        JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobWaitingId);

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
            jobWaiting.jobList.add(new Pair<JobHandler, Integer>(newJob, node));//Add to waiting job
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
        JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobHandler.getParentId());//Get waiting job
        if(jobWaiting == null) {
            //If a worker crashed, ignore all other results
            return null;
        }
        jobWaiting.newResult(jobHandler);
        if(jobWaiting.isDone()) {
            //Lock step, only release the nodes when all jobs are done!
            for(Pair<JobHandler, Integer> pair : jobWaiting.jobList) {
                state.activeWorkers.remove(pair.second);//worker is done
                state.passiveWorkers.add(pair.second);//worker is passive
            }
            state.jobsWaitingForExecutionResults.remove(jobWaiting.jobHander.getId());
        }

        //Only after added workers to active, call dispatcher
        dispatchJob();//Get first job waiting in FIFO manner
        return jobWaiting;
    }

    /**
     * Called when a worker is removed
     * @param workerId Worker to be removed
     */
    public void removeWorker(Integer workerId) {
        if(!state.passiveWorkers.remove(workerId)) {
            //it is executing a job
            //execute ALL jobs again, because it is lockstep
            for(String jobWaitingId : state.jobsWaitingForExecutionResults.keySet()) {
                boolean found = false;
                for( Pair<JobHandler, Integer> pair : state.jobsWaitingForExecutionResults.get(jobWaitingId).jobList) {
                    if(pair.second.equals(workerId)) {
                        //Found jobHandler which failed
                        found = true;
                    }
                }
                if(found) {
                    JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobWaitingId);
                    JobHandler jobHandler = jobWaiting.jobHander;
                    ActorRef client = state.jobClientMapping.get(jobWaitingId);

                    state.jobsWaitingForExecutionResults.remove(jobWaitingId);//remove from jobsWaiting
                    state.jobClientMapping.remove(jobWaitingId);//remove from client mapping
                    update(jobHandler, client);
                    break;
                }
            }
            state.activeWorkers.remove(workerId);//remove from active workers
        }
        state.workerIdToWorkerNode.remove(workerId);//remove from workerId mapping
    }
}
