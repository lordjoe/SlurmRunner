package com.lordjoe.ssh;

/**
 * com.lordjoe.ssh.ScriptGenerator
 * User: Steve
 * Date: 2/5/2020
 * this class write slurm scripts to execute a job
 */
public class ScriptGenerator {
       public final BlastLaunchDTO job;

       public ScriptGenerator(BlastLaunchDTO j)    {
           job = j;
       }

       

public static final String SEVEN_NODES =
        "#! /bin/bash\n" +
                "COUNTER=1\n" +
                "for file in $MULTI_BLAST_INPUT\n" +
                "do\n" +
                "  fileName=${file##*/}\n" +
                "  echo \"setting partition to CPU for job $COUNTER\"\n" +
                "  sbatch --exclude=node[001-027] --job-name=$COUNTER$fileName ./submitToOneNode.sh $file $COUNTER \n" +
                "  let COUNTER=COUNTER+1 \n" +
                "done\n" +
                "wait";


 public static final String SUBMIT_ONE_NODE =
               "#! /bin/bash\n" +
                       "#\n" +
                       "### $1 is the input for blast file name with path\n" +
                       "### $2 is the counter from the calling script, used for output\n" +
                       "#\n" +
                       "#SBATCH --ntasks=1\n" +
                       "#SBATCH --cpus-per-task=32\n" +
                       "#SBATCH --output=batchOutput$2.txt \n" +
                       " \n" +
                       "fileName=${1##*/}\n" +
                       "srun -n1 --exclusive BLAST_PROGRAN -query $1 -db DATA_BASE -num_threads 32 -num_descriptions 10 -max_target_seqs 10 -evalue 1E-09 -out ~/output-blast/output$fileName$2 &\n" +
                       "wait";


}
