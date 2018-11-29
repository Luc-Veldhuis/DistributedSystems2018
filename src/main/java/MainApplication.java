import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Actor;
//import JobHandler.Message;

public class MainApplication {

    public static void main(String[] args) throws IOException {

        //create the Actor
        ActorSystem root = ActorSystem.create("root-node");
        List<Integer> headNodeIds = Utils.getListOfLength(Configuration.NUMBER_OF_HEADNODES);
        List<Integer> workerIds = Utils.getListOfLength(Configuration.NUMBER_OF_WORKERS_IN_POOL);

        List<ActorRef> headNodes = new ArrayList<ActorRef>();

        try {
            // Create reference for top level actors (head nodes)
            for (int i = 0; i < headNodeIds.size(); i++) {
                ActorRef headRef = root.actorOf(HeadNode.props(headNodeIds.get(i)), "headNodeId-" + headNodeIds.get(i));
                headNodes.add(headRef);
            }
            createInitalWorkers(root, workerIds, headNodes);
            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } finally {
            root.terminate();//also terminates all other nodes
        }

    }

    public static void createInitalWorkers(ActorSystem root, List<Integer> workerIds, List<ActorRef> headNodes) {
        //create inital pool of workers, other processes can create these as well
        for(int i = 0; i < workerIds.size(); i++) {
            ActorRef workerRef = root.actorOf(WorkerNode.props(workerIds.get(i), headNodes), "workerId-" + workerIds.get(i));
        }
    }

}