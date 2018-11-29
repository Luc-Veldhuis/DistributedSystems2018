import akka.actor.AbstractActor;
//import akka.actor.ActorLogging;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerNode extends AbstractActor {
    public final Integer workerId;
    public List<ActorRef> headnodes;
    public Messages messages;

    public static Props props(Integer workerId, List<ActorRef> headnodes) {

        System.out.println("Worker node created");
        return Props.create(WorkerNode.class, () -> new WorkerNode(workerId, headnodes));
    }

    public WorkerNode(Integer workerId, List<ActorRef> headnodes) {
        this.workerId = workerId;
        this.headnodes = headnodes;
        this.messages = new Messages();

        System.out.println("workerNodeId: " + workerId);
        if(headnodes.size() > 1) {
            registerWorker(1);
        }
    }

    public void sendError(JobHandler job) {
        //TODO send error
    }

    public void sendResult(JobHandler job, Object result) {
        //TODO send result
    }

    public void executeJob(Messages.SendJobToWorker message) {
        //TODO run this
        try {
            Object result = message.job.job.invoke(this);
            sendResult(message.job, result);
        }  catch (IllegalAccessException e) {
            sendError(message.job);
        } catch (InvocationTargetException e) {
            sendError(message.job);
        }
    }

    public void registerWorker(int position) {
        //TODO I think there should be a catch around this
        ActorRef headnode = this.headnodes.get(position);
        headnode.tell(messages.registerWorkerToHead(this), this.self());
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                .match(
                        Messages.SendJobToWorker.class, this::executeJob
                )
                .build();
    }
}