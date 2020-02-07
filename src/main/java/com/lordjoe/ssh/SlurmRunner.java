package com.lordjoe.ssh;

import com.lordjoe.fasta.FastaTools;
import com.lordjoe.fasta.LocalJobRunner;
import com.lordjoe.utilities.FileUtilities;

import java.io.File;
import java.util.Properties;

/**
 * com.lordjoe.ssh.SlurmRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 */
public class SlurmRunner {


    public static BlastLaunchDTO handleLocBlastArgs(String[] args) {
        int index = 0;
        BLASTProgram program = BLASTProgram.BLASTP;
        String database = "xxx";
        String query = "xxx";
        String out = "xxx";
        if (args[index++].toLowerCase().endsWith("blastn"))
            program = BLASTProgram.BLASTN;     // todo get smarter handle more cases

        BlastLaunchDTO ret = new BlastLaunchDTO(program);
        while (index < args.length) {
            String next = args[index];
            if (next.equalsIgnoreCase("-db")) {
                index++;
                database = args[index++];
                continue;
            }
            if (next.equalsIgnoreCase("-remote")) {
                index++;
                continue;
            }
            if (next.equalsIgnoreCase("-query")) {
                index++;
                query = args[index++];
                continue;
            }
            if (next.equalsIgnoreCase("-out")) {
                index++;
                out = args[index++];
                continue;
            }
            if (next.equalsIgnoreCase("-outfmt")) {
                index++;
                index++;
                continue;
            }


        }
        ret.output = new File(out);
        ret.query = new File(query);
        ret.format = BLASTFormat.XML2;
        ret.database = database;
        return ret;

    }


    public final BlastLaunchDTO job;
    public final Properties clusterProperties = LocalJobRunner.getClusterProperties(null);

    public SlurmRunner(BlastLaunchDTO job) {
        this.job = job;
    }

    public  String generateSlurmScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("#! /bin/bash\n" +
                "#\n" +
                "### $1 is the input for blast file name with path\n" +
                "### $2 is the counter from the calling script, used for output\n" +
                "#\n" +
                "#SBATCH --ntasks=1\n" +
                "#SBATCH --cpus-per-task=32\n" +
                "#SBATCH --output=batchOutput$2.txt\n" +
                "\n" +
                "fileName=${1##*/}\n");
        sb.append("srun  -n1 --exclusive ") ;
        sb.append(generateExecutionScript());
        sb.append("\n");
        sb.append("wait\n");
        return sb.toString();
    }

    public  String generateExecutionScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("filename=${1}\n");
        sb.append("base=\"${filename%.*}\"\n");
        sb.append("mv base base,xml\n");

        String program = clusterProperties.getProperty("LocationOfBLASTPrograms") + job.program.toString().toLowerCase();
        sb.append(program);
        sb.append(" -query ");
        sb.append(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeInputDirectory")  + "/" +  job.id);
        sb.append("/");
        sb.append("${filename}");
        sb.append(" ");

        sb.append(job.database);
        sb.append(" -db ");
        sb.append(job.database);

        sb.append("   -num_threads 32 -num_descriptions 10 -num_alignments 10 -evalue 1E-09 ");

        sb.append(" outfmt ");
        sb.append(Integer.toString(job.format.code));
        sb.append(" ");
        
        sb.append(" -out ");
        sb.append(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeOutputDirectory")   + "/" +  job.id);
        sb.append("/");
        sb.append("${base}");

        return sb.toString();
    }

    public  String generateIterateScript() {
        StringBuilder sb = new StringBuilder();
          sb.append(" for file in ");
        sb.append(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeInputDirectory")  + "/" + job.id);
        sb.append("/*\n");

        sb.append("do\n");
        sb.append(" ./submitToCPUNode.sh $file \n");
         sb.append("done");

        return sb.toString();
    }


    public  String generateSlurmIterateScript() {
        StringBuilder sb = new StringBuilder();
         sb.append(" for file in ");
        sb.append(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeInputDirectory")  + "/" + job.id);
        sb.append("/*\n");

        sb.append("do");
        sb.append(" sbatch --job-name=$COUNTER$fileName ./submitToCPUNode.sh $file   ");
          sb.append("done");

        return sb.toString();
    }



    public   File writeExecutionScript() {
        File ScriptsDirectory =  new File(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeScriptDirectory") + "/"  +  job.id);
        ScriptsDirectory.mkdirs();
        File out = new File(ScriptsDirectory,"submitToCPUNode.sh") ;
        FileUtilities.writeFile(out,generateExecutionScript());
        return out;
    }


    public   File writeIterationScript() {
        File ScriptsDirectory =  new File(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeScriptDirectory") + "/"  +  job.id);
        ScriptsDirectory.mkdirs();
        File out = new File(ScriptsDirectory,"runBlast.sh") ;
        String data = generateIterateScript();
        FileUtilities.writeFile(out,data);
        return out;
    }

    public void splitQuery(File in)  {
        int numberEntries = FastaTools.countFastaEntities(in);
        int splitSize =  (numberEntries / 7);

        File outDirectory =  new File(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeInputDirectory")   + "/" +  job.id);
        outDirectory.mkdirs();

        String baseName = "splitFile";
        FastaTools.splitFastaFile(in, outDirectory, baseName, splitSize, numberEntries);

    }


    public static void main(String[] args) {
        BlastLaunchDTO dto = handleLocBlastArgs(args);
        SlurmRunner me = new SlurmRunner(dto);
        me.splitQuery(me.job.query);
        me.writeExecutionScript();
        me.writeIterationScript();
        System.out.println(me.job.id);
    }
}

