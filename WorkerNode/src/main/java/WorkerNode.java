import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkerNode extends AbstractActor {
    public final Integer workerId;
    public List<ActorSelection> headnodes;

    public static Props props(Integer workerId, String[] headnodes) {

        System.out.println("Worker node created");
        return Props.create(WorkerNode.class, () -> new WorkerNode(workerId, headnodes));
    }

    public WorkerNode(Integer workerId, String[] headnodes) {
        this.workerId = workerId;
        this.headnodes = new ArrayList<>();
        for(String node : headnodes) {
            this.headnodes.add(getContext().actorSelection(node));
        }

        System.out.println("workerNodeId: " + workerId);
        if(this.headnodes.size() >= 1) {
            registerWorker();
        }
    }

    WorkerData createMessageData() {
        return new WorkerData(this.getSelf(), this.workerId);
    }

    public void sendResult(JobHandler job) {
        for(ActorSelection node:headnodes) {
            node.tell(new SendJobDone(job,createMessageData()), this.self());
        }
    }

    public void executeJob(GetJobFromHead message) {
        try {
            message.job.setResult(message.job.job.get());
        }  catch (Exception e) {
            message.job.setException(e);
        }
        sendResult(message.job);
    }

    public void registerWorker() {
        for(ActorSelection node:headnodes) {
            node.tell(new RegisterWorkerToHead(createMessageData()), this.self());
        }
    }

    public void sendRemove() {
        //TODO maybe overkill to send it to each headnode
        for(ActorSelection node:headnodes){
            node.tell(new RemoveWorkerFromHead(createMessageData()), this.self());
        }
    }

    @Override
    public void postStop() {
        sendRemove();
    }


    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                        GetJobFromHead.class, this::executeJob
                )
                .build();
    }

    public static class RegisterWorkerToHead implements Serializable {
        public WorkerData workerNode;

        public RegisterWorkerToHead(WorkerData worker) {
            this.workerNode = worker;
        }
    }

    public static class RemoveWorkerFromHead implements Serializable {
        public WorkerData workerNode;

        public RemoveWorkerFromHead(WorkerData worker) {
            this.workerNode = worker;
        }
    }

    public static class GetJobFromHead implements Serializable {
        public JobHandler job;
        GetJobFromHead(JobHandler job) {
            this.job = job;
        }
    }

    public static class SendJobDone implements Serializable {
        public JobHandler jobHandler;
        public WorkerData workerNode;

        public SendJobDone(JobHandler worker, WorkerData workerNode) {
            this.jobHandler = worker;
            this.workerNode = workerNode;
        }
    }



}