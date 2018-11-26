import java.io.IOException;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;

public class MainApplication {

    public static void main(String[] args) throws IOException {
        ActorSystem system = ActorSystem.create("iot-system");

        try {
            // Create top level actor (head node)
            ActorRef supervisor = system.actorOf(CreateActor.props(), "head-node");
            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            system.terminate();
        }
    }

}