import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientActor<E> extends AbstractActor {

    ActorRef headNode;
    Messages messages;
    Consumer doneHander;
    Job job;
    Job job2;
    ArrayList<Job> jobList;
    public String[] headNodeUri;

    public static Props props(ActorRef headNode, String[] headNodeUri) {
        System.out.println("Client job created");
        return Props.create(ClientActor.class, () -> new ClientActor(headNode, headNodeUri));
    }

    public class MessageFromClientToHead{
            Job job;
        MessageFromClientToHead(Job job){
            this.job  = job;
        }
    }

    public ClientActor(ActorRef headNode, String[] headNodeUri) {

        jobList = new ArrayList<>();

        this.headNode = headNode;
        this.headNodeUri = headNodeUri;
        jobCreation();
        submitJob();
    }


    public void jobCreation(){
        job = new Job();
        job2 = new Job();

        jobList.add(job);
        //jobList.add(job2);

        System.out.println("job list: " + jobList);
        //job.setJob((Supplier<Integer>) this::sleep);
    }


    public void done(int result) {
        System.out.println(result);
    }

    public void submitJob(){
        for(Job job : jobList){
            MessageFromClientToHead m = new MessageFromClientToHead(job);
            headNode.tell( m, ActorRef.noSender());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    System.out.println(msg);
                })
                /*.match(
                    Messages.GetJobFromHead.class, this::receivedJob
                )*/.build();
    }
}
