public interface PolicyInterface {

    /**
     * Make sure that we can use different  Policys and reference this interface as Type
     * @param jobHandler
     * @param jobActor
     * @throws Exception
     */
    //We have to think about what functions a policy really needs

    //update when job comes in
    public void update(JobHandler jobHandler, JobActor jobActor) throws Exception;

    //update when job finishes
    public void update(JobHandler jobHandler, WorkerNode worker) throws Exception;

}
