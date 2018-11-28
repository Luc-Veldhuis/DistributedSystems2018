import java.io.IOException;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Actor;
//import JobHandler.Message;

public class MainApplication {

    public static void main(String[] args) throws IOException {

        //create the Actor
        ActorSystem system = ActorSystem.create("head-node");
        Integer[] headNodeId = {1,2,3};

        try {
            // Create reference for top level actor (head node)
            ActorRef headNode = system.actorOf(Props.create(JobHandler.class, headNodeId, 0, 5) );
            headNode.tell("Hi, I am the head node", Actor.noSender());
            headNode.tell(new JobHandler.Message(), Actor.noSender());
            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            system.terminate();
        }

    }

}