#!/bin/sh
#SBATCH --time=00:00:10
#SBATCH -N4
ids=$(echo $SLURM_JOB_NODELIST | grep -oP "(\d*)")
echo $ids
srun -N4 -l ./selector.sh $ids
[jvs275@fs0 DistributedSystems]$ cat selector.sh
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
