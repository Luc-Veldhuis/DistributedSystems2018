import akka.actor.ActorRef;

import java.io.Serializable;

/**Wrapper class
 * Used to store WorkerNode data
 */
public class WorkerData implements Serializable {

    ActorRef self;
    int workerId;

    WorkerData(ActorRef self, int id) {
        this.self = self;
        this.workerId = id;
    }

}
