import akka.actor.ActorRef;

import java.util.List;

public class Messages {

    public class RegisterWorkerToHead {
        public WorkerNode workerNode;

        public RegisterWorkerToHead(WorkerNode worker) {
            this.workerNode = worker;
        }
    }

    public class RemoveWorkerFromHead {
        public WorkerNode workerNode;

        public RemoveWorkerFromHead(WorkerNode worker) {
            this.workerNode = worker;
        }
    }


    public class CrashingHeadNode {
        public HeadNode headNode;
        public CrashingHeadNode(HeadNode headNode) {
            this.headNode = headNode;
        }
    }

    public class PropagateHeadNodes {
        public List<ActorRef> headNodes;
        public PropagateHeadNodes(List<ActorRef> headNodes) {
            this.headNodes = headNodes;
        }
    }



    public RegisterWorkerToHead registerWorkerToHead(WorkerNode worker) {
        return new RegisterWorkerToHead(worker);
    }

    public RemoveWorkerFromHead removeWorkerToHead(WorkerNode worker) {
        return new RemoveWorkerFromHead(worker);
    }





    public CrashingHeadNode crashingHeadNode(HeadNode headNode) {
        return new CrashingHeadNode(headNode);
    }

    public PropagateHeadNodes propagateHeadNodes(List<ActorRef> headNodes) {
        return new PropagateHeadNodes(headNodes);
    }



}
