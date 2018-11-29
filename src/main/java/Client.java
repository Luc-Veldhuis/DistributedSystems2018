import akka.actor.ActorRef;

public class Client {

    public void main(String[] args) {
        ActorRef headNode = null;//TODO create actorRef from url
        Job job = new Job(headNode);
    }
}
