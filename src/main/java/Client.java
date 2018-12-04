import akka.actor.ActorRef;
import akka.actor.ActorSelection;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Client {

    public String[] headNodeUri;

    public Client(String[] headNodeUri) {
        this.headNodeUri = headNodeUri;
    }

    public static void main(String[] args) {
        Client client = new Client(args);
        //client.execute();
    
}
