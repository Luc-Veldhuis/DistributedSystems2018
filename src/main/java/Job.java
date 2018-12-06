import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Job{

    private static int counter = 0;

   // private JobHandler<E> jobHandler;
   // private Consumer doneHandler;
    //public Consumer consumerJob;
    //public Supplier supplierJob;
    //ActorSystem root = ActorSystem.create("root-node");
    //private ActorSelection headNodeRef;

    public Job(){

    }

    public Integer run() {
        try {
            Thread.sleep(10000);
            System.out.println("Job executed");
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted");
        }
        return 10;
    }

}
