public interface PolicyInterface {

    //TODO use this to implement a policy
    //We have to think about what functions a policy really needs

    //update when job comes in
    public void update(JobHandler jobHandler, JobActor jobActor) throws Exception;

    //update when job finishes
    public void update(JobHandler jobHandler, WorkerNode worker) throws Exception;

}
