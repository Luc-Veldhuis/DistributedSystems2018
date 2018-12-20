#!/bin/bash

echo $1 $2 $3 $4 $5 $6 $7
if [ "$(hostname)" == "node$6" ]; then
    echo node$6
    echo headnode
    cd .././HeadNode/build/install/HeadNode/bin
    ./HeadNode $1 $2 $3 $4 $5
    echo "Headnode is done"
    sleep 40
    exit 0
else
    if [ "$(hostname)" != "node$7" ]; then
        echo workerNode
        cd .././WorkerNode/build/install/WorkerNode/bin
        sleep 30
        echo FirstSleep
        #sleep 30
        ./WorkerNode akka.tcp://root-node@node$6:2552/user/*
        echo "Worker is done"
        sleep 40
        exit 0
    else
        echo clientNode
        cd .././Client/build/install/Client/bin
        sleep 30
        echo FirstSleep Client
        sleep 5
        #for i in {1..10}
            #do
             #echo "Client number "$i
             ./Client akka.tcp://root-node@node$6:2552/user/* $1
        #done
        echo "Client is done"
        sleep 40
        exit 0
     fi
fi
