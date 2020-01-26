package com.lordjoe.ssh;

import java.util.UUID;

/**
 * com.lordjoe.ssh.BuildBlastFile
 * User: Steve
 * Date: 10/29/2019
 */
public class BuildBlastFile {
    public static final String HEADER = "#! /bin/bash\n" +
            "#\n" +
            "### $1 is the input for blast file name with path\n" +
            "### $2 is the counter from the calling script, used for output\n" +
            "#\n" +
            "#SBATCH --ntasks=1\n" +
            "#SBATCH --cpus-per-task=32\n" +
            "#SBATCH --output=batchOutput$2.txt ";

     public static final String BLAST_DIR = "/mnt/beegfs/proj/int/eva/sparkhydra/blast/";

     public static String buildScript()
     {
         StringBuilder sb = new StringBuilder();
         sb.append(HEADER);
         sb.append("\n");
         sb.append("srun -n1 --exclusive ");
         sb.append(BLAST_DIR);
         sb.append("bin/blastp");
         sb.append(" ");
         sb.append("-query ");
         sb.append(BLAST_DIR);
         sb.append("input-blast/LHCB1_70.fa");
         sb.append(" ");
         sb.append("-db ");
         sb.append(BLAST_DIR);
         sb.append("blastDBs/sorted.nr.viridiplantae");
         sb.append(" -num_threads 32 -num_descriptions 10 -num_alignments 10 -evalue 1E-09 ");
         sb.append("-out ");
         sb.append(BLAST_DIR);
         sb.append("/output-blast/test_output" + UUID.randomUUID().toString());
         sb.append("\n");

         return sb.toString();
     }

}
