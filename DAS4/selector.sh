#!/bin/bash
echo $6 $7
if [ "$(hostname)" == "node$6" ]; then
    echo node$6
    echo headnode
    cd .././HeadNode/build/install/HeadNode/bin
    ./HeadNode $1 $2 $3 $4 $5
    echo "Headnode is done"
else
    if [ "$(hostname)" != "node$7" ]; then
        echo workerNode
        cd .././WorkerNode/build/install/WorkerNode/bin
        sleep 30
        echo FirstSleep
        ./WorkerNode akka.tcp://root-node@node$6:2552/user/*
        echo "Worker is done"
    else
        echo clientNode
        cd .././Client/build/install/Client/bin
        sleep 30
        echo FirstSleep Client
        sleep 10
        echo SecondSleep
        #sleep 30
        #echo SixthSleep

        ./Client akka.tcp://root-node@node$6:2552/user/*
        echo "Client is done"
     fi
fi
