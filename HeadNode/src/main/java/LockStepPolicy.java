import akka.actor.ActorRef;
import akka.event.LoggingAdapter;

/**
 * Lock step policy, releases worker only after all corresponding jobs are done
 */
public class LockStepPolicy implements PolicyInterface {

    //Number every JobHandler
    int idCounter = 0;
    ActorRef headNode;
    HeadNodeState state;
    LoggingAdapter log;

    LockStepPolicy(HeadNodeState state, ActorRef headNode, LoggingAdapter log) {
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
            System.out.println("Not enough workers to dispatch job. Workers left: "+state.passiveWorkers.size());
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
            addFailures(newJob, jobWaiting.jobHander);

            //Get the first passive worker
            Integer node = state.passiveWorkers.get(0);
            state.passiveWorkers.remove(node);//remove first node
            state.activeWorkers.add(node); // add it to active

            ActorRef workerNodeRef = state.workerIdToWorkerNode.get(node);//Get actor reference
            jobWaiting.jobList.add(new Pair<JobHandler, Integer>(newJob, node));//Add to waiting job
            workerNodeRef.tell(new WorkerNode.GetJobFromHead(newJob), headNode);//Run job
            System.out.println("Send job "+newJob.getId()+" to worker node"+ node);
        }

    }

    private void addFailures(JobHandler newJob, JobHandler jobHander) {
        if(jobHander.numberOfByzantianFailures > 0) {
            newJob.numberOfByzantianFailures = 1;
            jobHander.numberOfByzantianFailures--;
        }
        if(jobHander.numberOfFailSilentFailures > 0) {
            newJob.numberOfFailSilentFailures = 1;
            jobHander.numberOfFailSilentFailures--;
        }
        if(jobHander.numberOfFailStopFailures > 0) {
            newJob.numberOfFailStopFailures = 1;
            jobHander.numberOfFailStopFailures--;
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
            state.activeWorkers.remove(workerId);//remove from active workers
            //it is executing a job
            //execute ALL jobs again, because it is lockstep
            System.out.println("Failing worker "+workerId+ " is active");
            for(String jobWaitingId : state.jobsWaitingForExecutionResults.keySet()) {
                boolean found = false;
                for( Pair<JobHandler, Integer> pair : state.jobsWaitingForExecutionResults.get(jobWaitingId).jobList) {
                    if(pair.second.equals(workerId)) {
                        //Found jobHandler which failed
                        found = true;
                    }
                }
                if(found) {
                    System.out.println("Restarting all jobs "+jobWaitingId);
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
