import akka.actor.ActorRef;

public class WorkerData {

    ActorRef self;
    int workerId;

    WorkerData(ActorRef self, int id) {
        this.self = self;
        this.workerId = id;
    }

}
