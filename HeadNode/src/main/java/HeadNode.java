import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class HeadNode extends AbstractActor {

    static int executed_jobs = Configuration.NUMBER_OF_JOBS;
    static int decrementJobs() {
        synchronized(HeadNode.class) {
            return --executed_jobs;
        }
    }



    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public final Integer headNodeId;
    public List<ActorRef> headNodes;
    Configuration config;
    /**
     * Any stuff relating to the state of the node should be stored in this class.
     */
    public HeadNodeState state;
    Scheduler scheduler;
    ByzantianChecker failCheck;
    int failingNodes = 0;
    boolean isBoss = false;

    /**
     * This actor handles all logic in scheduling, receiving and delegating jobs.
     * @param headNodeId the id of the HeadNode
     * @return returns new Actor
     */
    public static Props props(Integer headNodeId, Configuration config) {
        return Props.create(HeadNode.class, () -> new HeadNode(headNodeId, config));
    }

    public HeadNode(Integer headNodeId, Configuration config) {
        this.headNodeId = headNodeId;
        this.config = config;
        log.info("Headnode " + headNodeId+" created");
        if(headNodeId == 0){
            log.info("Headnode " + headNodeId+" is the boss");
            this.isBoss = true;
            this.state = new HeadNodeState();
            this.scheduler = new Scheduler(this.state, this.self(), this.config, this.log);
            this.failCheck = new ByzantianChecker(this.state, this.config);
        }
    }

    /**
     * Call used to register a WorkerNode to the HeadNode
     * @param message the register message
     */
    public void registerWorker(WorkerNode.RegisterWorkerToHead message) {
        if(this.isBoss) {
            int workerId = state.workerIdCounter;
            log.info("Headnode " + headNodeId+" registered worker " + workerId);
            getSender().tell(new WorkerNode.GetRegistrationResult(workerId), this.self());
            state.workerIdCounter++;
        }

        getContext().watch(getSender());//Watch the actors!
    }

    /**
     * Call used to confirm registration from WorkerNode to the HeadNode
     * @param message the register confirmation message
     */
    public void confirmRegistration(WorkerNode.ConfirmRegistrationResult message) {
        if(this.isBoss) {
            state.passiveWorkers.add(message.workerId);
            state.workerIdToWorkerNode.put(message.workerId, getSender());
            log.info("Headnode " + headNodeId+" confirmed registered worker " + message.workerId);
            scheduler.policy.dispatchJob();//If there were no workers left, check if we can run again
        }
    }

    /**
     * Call used when a Job is send by the client
     * @param message the message from the client
     */
    public void scheduleJob(JobActor.GetJobFromClient message) {
        if(this.isBoss) {
            log.info("Headnode " + headNodeId+" got new job " + message.jobHandler.debugId);
            if(message.jobHandler.crashHeadNodeWithId == this.headNodeId) {
                getContext().stop(this.self());
            } else {
                this.scheduler.update(message.jobHandler, getSender());
            }
        }
    }

    /**
     * Call used when a WorkerNode leaves the system
     * @param message the message from the worker when it leaves the system
     */
    public void removeWorker(WorkerNode.RemoveWorkerFromHead message) {
        if(this.isBoss) {
            log.info("Headnode " + headNodeId+" removed worker " + message.workerNode.workerId);
            scheduler.removeWorker(message.workerNode.workerId);
        }
    }

    /**
     * Once a job has run x times, check if there are any Byzantine errors
     * @param message Message from the head node to the client
     */
    public void checkJob(WorkerNode.SendJobDone message) {
        if(this.isBoss) {//only do this on 1 actor
            log.info("Headnode "+headNodeId+" received result of job "+message.jobHandler.getId());
            JobWaiting jobWaiting = this.scheduler.update(message.jobHandler, message.workerNode);//get waiting jobs
            if(jobWaiting != null && jobWaiting.isDone(config.NUMBER_OF_DUPLICATIONS)) {
                //log.info("JOB-FINISHING-MESSAGE ("+message.jobHandler.getParentId().length()+","+message.jobHandler.getParentId()+","+System.currentTimeMillis()+") @ : Headnode " + headNodeId+" done with job "+message.jobHandler.getParentId());


                JobHandler jobHandler = this.failCheck.check(jobWaiting);
                log.info("Headnode " + headNodeId+" job "+jobHandler.getId()+" checked");
                ActorRef client = state.jobClientMapping.get(jobHandler.getId());
                log.info("///HEADNODE-FINISHED: ("+message.jobHandler.debugId+","+ (System.currentTimeMillis() - message.jobHandler.originalCreationTime)+ ")"    );
                client.tell(new JobActor.GetJobFromHead(jobHandler), this.self());

                int num_left = decrementJobs();
                log.info("Jobs left to run in Headnode: "+num_left);
                if (num_left == 0) {

                    log.info("/////HEADNODE-IS-DONE: Done testing the workload");
                    Collection<ActorRef> workers = state.workerIdToWorkerNode.values();
                    for(ActorRef workerNode : workers) {
                        workerNode.tell(new WorkerNode.TerminateSystem(), ActorRef.noSender());
                        //getContext().stop(workerNode);

                    }
                    log.info("////HEADNODE-DONE. Sleeping for 2000 millis");

                    //wait for termination messaage to workers to get delivered
                    try{
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        log.info("Headnode sleep interrupted");
                    }

                    System.exit(0);
                    //getContext().stop(self());
                }
            }
        }
    }

    /**
     * Call when 1 of the head nodes has failed, it receives the HeadNodeState
     * @param message From other HeadNode when it fails
     */
    public void switchToBackupHeadNode(HeadNode.CrashingHeadNode message) {
        //another headnode went down, take over the state
        this.state = message.headNode.state;
        failingNodes++;
        if(failingNodes == this.headNodeId) {//because we use incremental ids, this works, might be more cleverly done
            log.info("Headnode "+headNodeId+" is the boss after update");
            this.isBoss = true;
            this.scheduler = new Scheduler(this.state, this.self(), this.config, this.log);
            this.failCheck = new ByzantianChecker(this.state, this.config);
            //watch all actors
            for(Integer workerId:state.activeWorkers) {
                getContext().watch(state.workerIdToWorkerNode.get(workerId));
            }
        }
    }

    /**
     * Call to receive the location of the other HeadNodes, should be received from the SpawnHeadNodes class
     * @param message Called in initialization fase to distribute location of other HeadNodes
     */
    public void updateHeadNodes(HeadNode.PropagateHeadNodes message) {
        //add more headnodes if they are added incrementally
        this.headNodes = message.headNodes;
    }

    /**
     * Used for gracefull failure, send the state to the other HeadNodes
     */
    @Override
    public void postStop() {
        //graceful failure, alert other headnodes
        if(headNodes == null) {
            return;
        }
        log.info("Headnode "+headNodeId+" going down, alerting others");
        for(ActorRef node : headNodes) {
            if(!this.self().equals(node)) {
                node.tell(new HeadNode.CrashingHeadNode(this), this.self());
            }
        }
    }

    public void checkHeartBeat(Terminated event) {
        if(this.isBoss) {
            ActorRef failingActor = event.actor();
            log.info("Headnode "+headNodeId+" seen some failure at location " + failingActor.path().toSerializationFormat());
            for (Integer workerId : state.workerIdToWorkerNode.keySet()) {
                ActorRef node = state.workerIdToWorkerNode.get(workerId);
                if (failingActor.equals(node)) {
                    log.info("Headnode "+headNodeId+" detected silent failure of worker: " + workerId);
                    scheduler.removeWorker(workerId);
                    break;
                }
            }
        }
    }


    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                        WorkerNode.RegisterWorkerToHead.class, this::registerWorker
                )
                .match(
                        WorkerNode.ConfirmRegistrationResult.class, this::confirmRegistration
                )
                .match(
                        WorkerNode.RemoveWorkerFromHead.class, this::removeWorker
                )
                .match(
                        JobActor.GetJobFromClient.class, this::scheduleJob
                )
                .match(
                        WorkerNode.SendJobDone.class, this::checkJob
                )
                .match(
                        HeadNode.CrashingHeadNode.class, this::switchToBackupHeadNode
                )
                .match(
                        HeadNode.PropagateHeadNodes.class, this::updateHeadNodes
                )
                .match(
                        Terminated.class, this::checkHeartBeat
                )
                .build();
    }

    /**
     * Message to receive when all HeadNodes are online
     */
    public static class PropagateHeadNodes implements Serializable {
        public List<ActorRef> headNodes;
        public PropagateHeadNodes(List<ActorRef> headNodes) {
            this.headNodes = headNodes;
        }
    }

    /**
     * Message to send when the HeadNode crashes
     */
    public static class CrashingHeadNode implements Serializable {
        public HeadNode headNode;
        public CrashingHeadNode(HeadNode headNode) {
            this.headNode = headNode;
        }
    }

}