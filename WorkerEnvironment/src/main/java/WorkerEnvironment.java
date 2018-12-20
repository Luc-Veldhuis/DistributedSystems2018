import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;


public class WorkerEnvironment {
    public static void main(String[] args) {
        // Creating environment
        ActorSystem.create("WorkerEnvironment", ConfigFactory.load());
    }
}
