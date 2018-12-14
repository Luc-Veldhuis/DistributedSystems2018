#/bin/bash
if [ "$(hostname)" == "node$1" ]
 then
    cd DistributedSystems2018/HeadNode
    gradle run
    #cd ../
    #cd WorkerNode
    #echo workerNode
fi
if [ "$(hostname)" == "node$2" ]
 then
    cd DistributedSystems2018/WorkerNode
    echo workerNode
    gradle run --args="akka.tcp://root-node@0.0.0.0:2552/user/headNodeId-0"
fi


# if [ "$(hostname)" == "node$3" ]
#  then
#     echo clientNode
# fi
