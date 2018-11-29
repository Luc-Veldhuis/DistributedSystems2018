import akka.actor.ActorRef;

public class Policy implements PolicyInterface {

    //TODO we can use this to quicky implement different policies
    HeadNodeState state;

    int idCounter = 0;
    Messages messages = new Messages();
    ActorRef headNode;

    @Override
    public void update(JobHandler jobHandler, ClientActor clientActor) throws Exception{
        //added
        if(state == null) {
            throw new Exception();
        }
        jobHandler.setId(idCounter+"");
        state.jobIdToJobHandler.put(jobHandler.getId(), jobHandler);
        if(state.passiveWorkers.size() >= Configuration.NUMBER_OF_BYZANTIAN_ERRORS){
            dispatchJob(jobHandler);
        } else {
            state.jobsWaiting.add(jobHandler.getId());
        }
        idCounter++;

    }

    public void dispatchJob(JobHandler jobHandler) {
        //Spawn x jobsWaiting
        state.jobsWaiting.remove(jobHandler.getId());
        state.jobsReadyForChecking.add(jobHandler.getId());
        for(int i = 0; i < Configuration.NUMBER_OF_BYZANTIAN_ERRORS; i++) {
            JobHandler newJob = new JobHandler(jobHandler.job);
            newJob.setId(jobHandler.getId()+"-"+i);

            int node =  state.passiveWorkers.remove(0);
            ActorRef workerNodeRef = state.workerIdToWorkerNode.get(node);
            state.activeWorkers.add(node);

            workerNodeRef.tell(messages.sendJobToWorker(newJob),headNode);

            state.jobsRunning.add(newJob.getId());
        }
    }

    @Override
    public void update(JobHandler jobHandler, WorkerNode workerNode) throws Exception {
        //done
        if(state == null) {
            throw new Exception();
        }
        state.jobsRunning.remove(jobHandler.getId());
        state.activeWorkers.remove(workerNode.workerId);
        state.passiveWorkers.add(workerNode.workerId);
        //TODO use the state.jobIdToJobHandler to update the status, maybe failchecker should be here?
        //TODO call dispatch Job with some job from the queue if enough free workers are available
    }
}
