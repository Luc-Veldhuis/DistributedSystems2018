#!/bin/bash
echo $6 $7
if [ "$(hostname)" == "node$6" ]; then
    echo node$6
    echo headnode
    python replacer.py node$6
    cd ../HeadNode 
    gradle build
    gradle install
    cd .././HeadNode/build/install/HeadNode/bin
    ./HeadNode $1 $2 $3 $4 $5
else
    if [ "$(hostname)" != "node$7" ]; then
        echo workerNode
        cd .././WorkerNode/build/install/WorkerNode/bin
        sleep 30
        ./WorkerNode akka.tcp://root-node@node$6:2552/user/*
    else
        echo clientNode
        cd .././Client/build/install/Client/bin
        sleep 50
        ./Client akka.tcp://root-node@node$6:2552/user/*
     fi
fi
