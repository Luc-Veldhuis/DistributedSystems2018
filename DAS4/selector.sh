#!/bin/bash
if [ "$(hostname)" == "node$1" ]; then
    echo headnode
    cd .././HeadNode/build/install/HeadNode/bin
    ./HeadNode
else
    if [ "$(hostname)" != "node$2" ]; then
        echo workerNode
        cd .././WorkerNode/build/install/WorkerNode/bin
        ./WorkerNode akka.tcp://root-node@node$1:2552/user/*
    else
        echo clientNode
        cd .././Client/build/install/Client/bin
        ./Client akka.tcp://root-node@node$2:2552/user/*
    fi
fi
