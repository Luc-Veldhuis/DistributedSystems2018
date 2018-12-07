import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import akka.actor.Actor;


public class JobHandler<E> implements Serializable {

    public SerializableSupplier job;
    public E result;
    public Exception e;
    private String id;
    public String parentId;
    public boolean done = false;

    /**
     * Object storing all functions to use on the head node, MUST BE SERIALIZABLE!!!
     * @param job
     */
    public JobHandler(SerializableSupplier job) {
        this.job = job;
    }

    /**
     * Used to update the result, only to be called by the worker
     * @param result
     */
    public void setResult(E result) {
        this.result = result;
        this.done = true;
    }
    /**
    Function to be called by the scheduler
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Function to be called by the scheduler
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * If a job is executed multiple times, it should be cloned and it can be used to set the original job
     * @param id
     */
    public void setParentId(String id) {
        this.parentId = id;
    }

    /**
     * Gets the parent ID of the clone
     * @return
     */
    public String getParentId() {
        return this.parentId;
    }

    /**
     * Used to get the response, can be an exception or a real result
     * @return
     * @throws Exception
     */
    public E getResult() throws Exception {
        //TODO how to implement this, not sure if this is the way
        if(this.e != null) {
            throw this.e;
        }
        return this.result;
    }

    /**
     * Used to set a possible exception, only to be called by the worker
     * @param e
     */
    public void setException(Exception e) {
        this.e = e;
        this.done = true;
    }

}
