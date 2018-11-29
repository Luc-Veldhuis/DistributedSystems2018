import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import akka.actor.Actor;


public class JobHandler<E> {

    public Supplier job;
    private E result;
    private Exception e;
    private String id;
    public int checked = 0;
    private boolean done = false;

    public JobHandler(Supplier job) {
        this.job = job;
    }

    public void setResult(E result) {
        this.result = result;
        this.done = true;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public E getResult() throws Exception {
        //TODO how to implement this, not sure if this is the way
        if(this.e != null) {
            throw this.e;
        }
        return this.result;
    }

    public void setException(Exception e) {
        this.e = e;
        this.done = true;
    }

}
