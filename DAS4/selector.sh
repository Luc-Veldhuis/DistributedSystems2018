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
        echo FirstSleep
        sleep 20
        echo SecondSleep
        #sleep 30
        #echo ThirdSleep
        ./WorkerNode akka.tcp://root-node@node$6:2552/user/*
    else
        echo clientNode
        cd .././Client/build/install/Client/bin
        sleep 30
        echo FirstSleep Client
        sleep 25
        echo SecondSleep
        #sleep 30
        #echo ThirdSleep
        #sleep 30
        #echo FourthSleep
        #sleep 30
        #echo FifthSleep
        #sleep 30
        #echo SixthSleep

        ./Client akka.tcp://root-node@node$6:2552/user/*
     fi
fi
