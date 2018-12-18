import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import akka.actor.Actor;


public class JobHandler<K,E> implements Serializable {

    public SerializableSupplier job;
    public SerializableFunction<K,E> functionJob;
    public E result;
    public K input;
    public Exception e;
    private String id;
    public String parentId;
    public boolean done = false;
    public int debugId;

    public long originalCreationTime;
    //public long finishedTime;
    public long workerIncomingTimestamp;


    //For debug purpouses
    public int numberOfByzantianFailures = 0;
    public int numberOfFailStopFailures = 0;
    public int numberOfFailSilentFailures = 0;
    public int crashHeadNodeWithId = -1;

    /**
     * Object storing all functions to use on the head node, MUST BE SERIALIZABLE!!!
     * @param job
     */
    public JobHandler(SerializableSupplier job) {
        this.job = job;
        this.debugId = this.hashCode();
        this.parentId = Long.toString(debugId);

    }

    /**
     * Object storing all functions to use on the head node, MUST BE SERIALIZABLE!!!
     * @param functionJob
     * @param input
     */
    public JobHandler(SerializableFunction functionJob, K input) {
        this.functionJob = functionJob;
        this.input = input;
        this.debugId = this.hashCode();
        this.parentId = Long.toString(debugId);
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
        if(id.startsWith(Integer.toString(debugId)))
            this.id = id;
        else
            this.id = debugId+"-"+id;
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
        //System.out.println(id);
        if(id != null && id.startsWith(Integer.toString(debugId)))
            this.parentId = id;
        else
            this.parentId = debugId+"-"+id;
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

    @Override
    public JobHandler<K,E> clone() {
        JobHandler<K, E> result;
        if(this.job == null) {
            result = new JobHandler<K, E>(this.functionJob, this.input);
        } else {
            result = new JobHandler<K, E>(this.job);
        }
        result.input = this.input;
        result.setId(this.getId());
        result.e = this.e;
        result.result = this.result;
        result.setParentId(this.getParentId());
        result.debugId = this.debugId;
        result.originalCreationTime = this.originalCreationTime;
        //result.finishedTime = this.finishedTime;

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JobHandler) {
            JobHandler jobHandler = (JobHandler)obj;
            boolean exceptionEqual = (this.e == null && jobHandler.e == null) || (this.e != null && jobHandler.e != null && this.e.getClass().equals(jobHandler.e.getClass()));
            boolean resultEqual = (this.result == null && jobHandler.result == null) || (this.result != null && jobHandler.result != null && this.result.equals(jobHandler.result));
            return exceptionEqual && resultEqual;

        } else {
            return super.equals(obj);
        }
    }
}
