import akka.actor.ActorRef;
import akka.event.LoggingAdapter;

/**
 * Lock step policy, releases worker only after all corresponding jobs are done
 */
public class LockStepPolicy extends Policy {

    //Number every JobHandler
    int idCounter = 0;
    ActorRef headNode;
    HeadNodeState state;
    LoggingAdapter log;
    Configuration config;

    LockStepPolicy(HeadNodeState state, ActorRef headNode, Configuration config, LoggingAdapter log) {
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
        log.info("Job "+ jobHandler.getId() + " has errors: "+ jobHandler.numberOfByzantianFailures + " "+ jobHandler.numberOfFailStopFailures + " "+ jobHandler.numberOfFailSilentFailures);
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
        if(!(state.passiveWorkers.size() >= config.NUMBER_OF_DUPLICATIONS)) {
            //System.out.println("Not enough workers to dispatch job. Workers left: "+state.passiveWorkers.size());
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
        for (int i = 0; i < config.NUMBER_OF_DUPLICATIONS; i++) {
            //Clone original job into x copies
            JobHandler newJob = jobWaiting.jobHander.clone();
            newJob.setId(jobWaiting.jobHander.getId() + "-" + i);
            newJob.setParentId(jobWaiting.jobHander.getId());
            addFailures(newJob, jobWaiting.jobHander);

            //Get the first passive worker
            Integer node = state.passiveWorkers.get(0);
            state.passiveWorkers.remove(node);//remove first node
            state.activeWorkers.add(node); // add it to active

            ActorRef workerNodeRef = state.workerIdToWorkerNode.get(node);//Get actor reference
            jobWaiting.jobList.add(new Pair<JobHandler, Integer>(newJob, node));//Add to waiting job
            workerNodeRef.tell(new WorkerNode.GetJobFromHead(newJob), headNode);//Run job
            log.info("Send job "+newJob.getId()+" to worker node "+ node);
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
        if(jobWaiting.isDone(config.NUMBER_OF_DUPLICATIONS)) {
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
            state.activeWorkers.remove(workerId);//remove from active workers
            //it is executing a job
            //execute ALL jobs again, because it is lockstep
            log.info("Failing worker "+workerId+ " is active");
            for(String jobWaitingId : state.jobsWaitingForExecutionResults.keySet()) {
                boolean found = false;
                for( Pair<JobHandler, Integer> pair : state.jobsWaitingForExecutionResults.get(jobWaitingId).jobList) {
                    if(pair.second.equals(workerId) && !pair.first.done) {
                        //Found jobHandler which failed
                        found = true;
                    }
                }
                if(found) {
                    log.info("Restarting all jobs "+jobWaitingId);
                    JobWaiting jobWaiting = state.jobsWaitingForExecutionResults.get(jobWaitingId);
                    JobHandler jobHandler = jobWaiting.jobHander;
                    ActorRef client = state.jobClientMapping.get(jobWaitingId);

                    //Remove all jobs which are also running/done
                    for(Pair<JobHandler, Integer> pair : jobWaiting.jobList) {
                        if(!pair.second.equals(workerId)) {
                            state.activeWorkers.remove(pair.second);//worker is done
                            state.passiveWorkers.add(pair.second);//worker is passive
                        }
                    }
                    state.jobsWaitingForExecutionResults.remove(jobWaiting.jobHander.getId());
                    state.jobClientMapping.remove(jobWaitingId);//remove from client mapping
                    update(jobHandler, client);
                    break;
                }
            }
        }
        state.workerIdToWorkerNode.remove(workerId);//remove from workerId mapping
    }
}
