import akka.actor.ActorRef;

public interface PolicyInterface {

    /**
     * Make sure that we can use different  Policys and reference this interface as Type
     * @param jobHandler
     * @param jobActor
     */
    //We have to think about what functions a policy really needs

    //update when job comes in
    public void update(JobHandler jobHandler, ActorRef jobActor);

    //update when job finishes
    public JobWaiting update(JobHandler jobHandler, WorkerData worker);

    //
    public void removeWorker(Integer workerId);

}
