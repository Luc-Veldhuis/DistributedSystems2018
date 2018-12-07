import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;
import java.util.Date;

public class Job{

    private static int counter = 0;
    private final long PERIOD = 1000L; // Adjust to suit timing


   // private JobHandler<E> jobHandler;
   // private Consumer doneHandler;
    //public Consumer consumerJob;
    //public Supplier supplierJob;
    //ActorSystem root = ActorSystem.create("root-node");
    //private ActorSelection headNodeRef;

    public Job(){

    }

    public Integer run() {
        //Timeout timeout = Timeout.create(Duration.ofSeconds(5));
        //sleep(5000);
        System.out.println("Job executed");

        /*try {
            //sleep(3000);
            TimeUnit.SECONDS.sleep(3);
            System.out.println("Job executed");
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted");
        }*/
        return 10;
    }

    public void sleep(int delay) {//Called every "Tick"
        long starTime;
        long thisTime = System.currentTimeMillis();
        starTime = thisTime;
        while(true) {
            if ((thisTime - starTime) <= delay) {
                thisTime = System.currentTimeMillis();;
            } else {
                return;
            }
        }
    }

}
