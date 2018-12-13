#!/bin/sh
#SBATCH --time=00:00:10
#SBATCH -N4
ids=$(echo $SLURM_JOB_NODELIST | grep -oP "(\d*)")
echo $ids
srun -N4 -l ./selector.sh $ids
