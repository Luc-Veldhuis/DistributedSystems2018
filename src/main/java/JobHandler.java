import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import akka.actor.Actor;


public class JobHandler {

    public Method job;

    public void setJob(Method job) {
        this.job = job;
    }

    public void getResult() {
        //TODO how to implement this, not sure if this is the way
    }
}
