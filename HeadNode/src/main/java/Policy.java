import akka.actor.ActorRef;

public class Policy implements PolicyInterface {

    //TODO we can use this to quicky implement different policies
    HeadNodeState state;

    int idCounter = 0;
    ActorRef headNode;

    /**
     * Used to update the schedule when a client job comes in
     * @param jobHandler
     * @param jobActor
     */
    @Override
    public void update(JobHandler jobHandler, JobActor jobActor) {
        //added
        if(state == null) {
            throw new InstantiationError();
        }
        jobHandler.setId(idCounter+"");
        JobWaiting jobWaiting = new JobWaiting(jobHandler);
        state.jobsWaiting.put(jobWaiting.jobHander.getId(),jobWaiting);
        dispatchJob(jobWaiting);
        idCounter++;

    }

    /**
     * Used to send a job to a WorkerNode
     * @param jobWaiting
     */
    public void dispatchJob(JobWaiting jobWaiting) {
        if(!(state.passiveWorkers.size() >= Configuration.NUMBER_OF_BYZANTIAN_ERRORS)) {
            return;
        }
        //Spawn x jobsWaiting
        state.jobsWaiting.remove(jobWaiting.jobHander.getId());
        state.jobsReadyForChecking.put(jobWaiting.jobHander.getId(), jobWaiting);
        for (int i = 0; i < Configuration.NUMBER_OF_BYZANTIAN_ERRORS; i++) {
            JobHandler newJob = new JobHandler(jobWaiting.jobHander.job);
            newJob.setId(jobWaiting.jobHander.getId() + "-" + i);
            newJob.setParentId(jobWaiting.jobHander.getId());

            int node = state.passiveWorkers.get(0);
            state.passiveWorkers.remove(node);//remove first node
            state.activeWorkers.add(node); // add it to active

            ActorRef workerNodeRef = state.workerIdToWorkerNode.get(node);
            workerNodeRef.tell(new WorkerNode.GetJobFromHead(newJob), headNode);

            state.jobsRunning.add(newJob.getId());
        }

    }

    /**
     * Called when a WorkerNode is finished
     * @param jobHandler
     * @param workerNode
     * @throws Exception
     */
    @Override
    public void update(JobHandler jobHandler, WorkerNode workerNode) throws Exception {
        //done
        if(state == null) {
            throw new Exception();
        }
        state.jobsRunning.remove(jobHandler.getId());//Job is done
        state.activeWorkers.remove(workerNode.workerId);//worker is done
        state.passiveWorkers.add(workerNode.workerId);//worker is passive
        JobWaiting jobWaiting = state.jobsReadyForChecking.get(jobHandler.getParentId());
        jobWaiting.newResult(jobHandler);
        //TODO use failchecker here
        if(!state.jobsWaiting.isEmpty()) {
            dispatchJob(state.jobsWaiting.get(state.jobsWaiting.keySet().toArray()[0]));
        }
        if(jobWaiting.isDone()) {
            //headNode.tell(WorkerNode.GetJobFromWorker(jobWaiting.jobHander, workerNode.createMessageData()), workerNode.self());
        }
    }
}
