import akka.actor.ActorRef;

import java.util.List;
import java.util.TimerTask;
import java.util.Date;

public class TaskScheduler extends TimerTask {

    List<ActorRef> headNodes;

    TaskScheduler(List<ActorRef> headNodes) {
        this.headNodes = headNodes;
    }

    Date current; //display current time

    //add task here
    public void run() {
        current = new Date(); // get the current time
        System.out.println("Current time is :" + current); // Display current time
        String headNodePaths[] = new String[headNodes.size()];
        for(int i = 0; i < headNodes.size(); i++) {
            ActorRef headNode = headNodes.get(i);
            headNodePaths[i] = headNode.path().toSerializationFormatWithAddress(headNode.path().address());
        }
        Client client = new Client(headNodes.get(0));
        client.execute();

    }
}
