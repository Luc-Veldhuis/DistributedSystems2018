import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import java.util.Timer;
import java.util.function.Supplier;

public class MainApplication {

    public static void main(String[] args) throws IOException, InterruptedException{

        //create the Actor
        ActorSystem root = ActorSystem.create("root-node");
        //ActorRef headRef;
        List<Integer> headNodeIds = Utils.getListOfLength(Configuration.NUMBER_OF_HEADNODES);
        List<Integer> workerIds = Utils.getListOfLength(Configuration.NUMBER_OF_BYZANTIAN_ERRORS);

        List<ActorRef> headNodes = new ArrayList<ActorRef>();
        List<ActorRef> workerNodes = new ArrayList<ActorRef>();

        try {
            // Create reference for top level actors (head nodes)
            for (int i = 0; i < headNodeIds.size(); i++) {
                ActorRef headRef = root.actorOf(HeadNode.props(headNodeIds.get(i)), "headNodeId-" + headNodeIds.get(i));
                headNodes.add(headRef);
            }
            createInitalWorkers(root, workerIds, headNodes, workerNodes);

            String headNodePaths[] = new String[headNodes.size()];
            for(int i = 0; i < headNodes.size(); i++) {
                ActorRef headNode = headNodes.get(i);
                headNodePaths[i] = headNode.path().toSerializationFormatWithAddress(headNode.path().address());
                System.out.println(headNodePaths[i]);
            }
            //ActorRef headRef = root.actorSelection(headNodePaths[0]);
            // create client actor
            ActorRef clientActor = root.actorOf(ClientActor.props(headNodes.get(0), headNodePaths), "list");



            //Client client = new Client(headNodePaths);
            //client.execute();

            //runScheduledTask(headNodes);
            //System.out.println("Press ENTER to exit the system");
            //System.in.read();
        } finally {
            root.terminate();//also terminates all other nodes
        }

    }

    public static void createInitalWorkers(ActorSystem root, List<Integer> workerIds, List<ActorRef> headNodes,  List<ActorRef> workerNodes) {
        //create inital pool of workers, other processes can create these as well
        for(int i = 0; i < workerIds.size(); i++) {
            ActorRef workerRef = root.actorOf(WorkerNode.props(workerIds.get(i), headNodes), "workerId-" + workerIds.get(i));
            workerNodes.add(workerRef);
        }
        HeadNode.acceptWorker(workerNodes);
    }

/*    public static void runScheduledTask(List<ActorRef> headNodes) throws InterruptedException{
        Timer time = new Timer(); //timer object
        TaskScheduler task = new TaskScheduler(headNodes); // taskScheduler object
        time.schedule(task, 0, 3000); // create task every 3 sec

        for (int i = 0; i <= 5; i++) {
            System.out.println("Execution in Main Thread...." + i);
            Thread.sleep(2000); //sleep for 2 sec
            if (i == 5) {
                System.out.println("Termination. Goodbye");
                System.exit(0);
            }
        }
    }*/

}