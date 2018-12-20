#!/bin/bash

## declare an array variable
declare -a policies=("LOCK_STEP" "MAXIMIZE" "SAME_MACHINE")
declare -a supported_byzantine=("0" "2")
declare -a stop_failures=("0" "0.05" "0.1")
declare -a silent_failures=("0.05" "0.1")
jobId="-1"
## now loop through the above array
for policy in "${policies[@]}"
do
    for byzantine in "${supported_byzantine[@]}"
    do
        for stop_failure in "${stop_failures[@]}"
        do
            if [ "$jobId" == "-1" ]; then
                echo "Executing: sbatch ./scriptToSubmitToScheduler.sh "$byzantine" "$stop_failure" 0 0.5 "$policy" "
                jobId=$(sbatch ./scriptToSubmitToScheduler.sh "$byzantine" "$stop_failure" 0 0.5 "$policy" | grep -oP "(\d+)")
            else
                echo "Executing: sbatch --dependency=afterok:$jobId  ./scriptToSubmitToScheduler.sh "$byzantine" "$stop_failure" 0 0.5 "$policy" "
                jobId=$(sbatch --dependency=afterany:$jobId ./scriptToSubmitToScheduler.sh "$byzantine" "$stop_failure" 0 0.5 "$policy" | grep -oP "(\d+)")
            fi
        done
        for silent_failure in "${silent_failures[@]}"
        do
            if [ "$jobId" == "-1" ]; then
                echo "Executing: sbatch ./scriptToSubmitToScheduler.sh "$byzantine" 0 "$silent_failure" 0.5 "$policy" "
                jobId=$(sbatch ./scriptToSubmitToScheduler.sh "$byzantine" 0 "$silent_failure" 0.5 "$policy"| grep -oP "(\d+)")
            else
            echo "Executing: sbatch --dependency=afterok:$jobId ./scriptToSubmitToScheduler.sh "$byzantine" 0 "$silent_failure" 0.5 "$policy" "
                jobId=$(sbatch --dependency=afterany:$jobId ./scriptToSubmitToScheduler.sh "$byzantine" 0 "$silent_failure" 0.5 "$policy"| grep -oP "(\d+)")
            fi
            echo $jobId
        done
    done
done
#sbatch ./scriptToSubmitToScheduler.sh NUMBER_OF_DUPLICATIONS RATE_OF_STOP_FAILURES RATE_OF_SILENT_FAILURES RATE_OF_BYZANTINE_FAILURES POLICY{LOCK_STEP, MAXIMIZE, SAME_MACHINE}