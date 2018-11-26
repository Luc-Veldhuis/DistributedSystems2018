import akka.actor.AbstractActor;
//import akka.actor.ActorLogging;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class CreateActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(CreateActor.class, CreateActor::new);
    }

    @Override
    public void preStart() {
        log.info("Actor Application started");
    }

    @Override
    public void postStop() {
        log.info("Actor Application stopped");
    }

    // basic necessity to create an Actor.
    //return the Received message
    //send a message to the Actor and print it out
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                    .match(String.class, msg -> {
                        System.out.println(msg);
                    }).build();
    }

}