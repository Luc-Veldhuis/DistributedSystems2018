#!/bin/bash
if [ "$(hostname)" == "node$1" ]; then
    echo headnode
else
    if [ "$(hostname)" != "node$2" ]; then
        echo workerNode
    else
        echo clientNode
    fi
fi
