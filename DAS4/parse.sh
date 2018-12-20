#!/bin/bash
touch workers
touch client
touch headnode

outdir=out-$1

mkdir $outdir

cat $1 | grep -o 'WORKER-FINISHED-JOB:.*)'  | sed -e 's/WORKER-FINISHED-JOB: (//' | sed -e 's/)//g' | sed -e 's/-[0-9]*,/,/g'   > $outdir/workers-$1.csv

cat $1 | grep -o 'WORKER-STOP-FAILURE:.*)'  | sed -e 's/WORKER-STOP-FAILURE: (//' | sed -e 's/)//g' | sed -e 's/-[0-9]*,/,/g'   > $outdir/workers-STOP-failure-$1.csv
cat $1 | grep -o 'WORKER-STOP-FAILURE:.*)'  | sed -e 's/WORKER-STOP-FAILURE: (//' | sed -e 's/)//g' | sed -e 's/-[0-9]*,/,/g'   >> $outdir/workers-$1.csv

cat $1 | grep -o 'WORKER-SILENT-FAILURE:.*)'| sed -e 's/WORKER-SILENT-FAILURE: (//' | sed -e 's/)//g' | sed -e 's/-[0-9]*,/,/g' > $outdir/workers-SILENT-failure-$1.csv
cat $1 | grep -o 'WORKER-SILENT-FAILURE:.*)'| sed -e 's/WORKER-SILENT-FAILURE: (//' | sed -e 's/)//g' | sed -e 's/-[0-9]*,/,/g' >> $outdir/workers-$1.csv

cat $1 | grep -o 'CLIENT-FINISHED-JOB:.*)'  | sed -e 's/CLIENT-FINISHED-JOB: (//' | sed -e 's/)//g' > $outdir/client-$1.csv

cat $1 | grep -o 'HEADNODE-FINISHED:.*)'  | sed -e 's/HEADNODE-FINISHED: (//' | sed -e 's/)//g' > $outdir/headnode-$1.csv

mv $1 $outdir/$1
