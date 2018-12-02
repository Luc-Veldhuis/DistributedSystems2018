import java.util.TimerTask;
import java.util.Date;

public class TaskScheduler extends TimerTask {

    Date current; //display current time

    //add task here
    public void run() {
        current = new Date(); // get the current time
        System.out.println("Current time is :" + current); // Display current time
    }
}
