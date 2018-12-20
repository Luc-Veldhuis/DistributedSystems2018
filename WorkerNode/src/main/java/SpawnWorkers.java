import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.util.List;

public class SpawnWorkers {

    /**
     * Used to spawn multiple workers on a remote system
     * @param args
     */
    public static void main(String[] args) {
        List<Integer> workerIds = Utils.getListOfLength(Configuration.NUMBER_OF_WORKERS_PER_SYSTEM);
        ActorSystem root = ActorSystem.create("root-node");
        for(int i = 0; i < workerIds.size(); i++) {
            ActorRef workerRef = root.actorOf(WorkerNode.props(args), "workerId-" + workerIds.get(i));
        }
    }
}
