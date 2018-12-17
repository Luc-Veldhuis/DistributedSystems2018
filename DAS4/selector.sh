#!/bin/bash
if [ "$(hostname)" == "node$6" ]; then
    echo node$1
    echo headnode
    cd ../HeadNode 
    gradle build
    gradle install
    cd ../DAS4
    python replacer.py node$1
    cd .././HeadNode/build/install/HeadNode/bin
    ./HeadNode $1 $2 $3 $4 $5
else
    if [ "$(hostname)" != "node$7" ]; then
        echo workerNode
        cd .././WorkerNode/build/install/WorkerNode/bin
        sleep 20
        ./WorkerNode akka.tcp://root-node@node$6:2552/user/*
    else
        echo clientNode
        cd .././Client/build/install/Client/bin
        sleep 40
        ./Client akka.tcp://root-node@node$6:2552/user/*
     fi
fi
