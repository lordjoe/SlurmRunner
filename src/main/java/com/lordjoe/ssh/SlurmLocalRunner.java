package com.lordjoe.ssh;

import com.lordjoe.blast.BLastTools;
import com.lordjoe.blast.OSValidator;
import com.lordjoe.fasta.LocalJobRunner;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * com.lordjoe.ssh.SlurmRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 */
public class SlurmLocalRunner implements IJobRunner  {


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
        ret.output = out;
        ret.query = new File(query);
        ret.format = BLASTFormat.XML2;
        ret.database = database;
        return ret;

    }


    public final BlastLaunchDTO job;
    public Map<String, Object> parameters = new HashMap<>();
    public final Properties clusterProperties = LocalJobRunner.getClusterProperties(null);
    private JobState lastState;
    private final AtomicReference<JobState> state = new AtomicReference<>();

    public SlurmLocalRunner(BlastLaunchDTO job, Map<String,? extends Object>  param) {
        this.job = job;
         state.set(JobState.RunStarted);
        IJobRunner.registerRunner(this);
        filterProperties(param);
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



    public JobState getState() {
        return state.get();
    }
    public String getId() {
        return this.job.id;
    }
    public BlastLaunchDTO getJob() { return job;}

    @Override
    public void setLastState(JobState s) {
        lastState = s;
    }

    @Override
    public JobState getLastState() {
        return lastState;
    }

    public Map<String, ? extends Object> filterProperties(Map<String, ? extends Object> in) {
        Map<String, Object> ret = new HashMap<>();
        String[] added = BLASTProgram.relevantProperties(getJob().program);
        String prefix =  BLASTProgram.prefix(getJob().program);
        for (int i = 0; i < added.length; i++) {
            String s = added[i];
            Object value = in.get(s);
            if(value != null) {
                String key = s.substring(prefix.length());
                ret.put(key, value);
                parameters.put(key,  value);
            }
        }
        String email = (String)in.get("email") ;
        if(email != null)
            parameters.put("email",  email);
        return ret;
    }


    public  String generateExecutionScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("filename=${1}\n");
        sb.append("base=`basename \"$filename\"`\n");
        sb.append("base1=${base%.*}\n");
        sb.append("base=${base1}.xml\n");

        String program = clusterProperties.getProperty("LocationOfLocalBLASTPrograms") + job.program.toString().toLowerCase();
        sb.append(program);
        sb.append(" -query ");

        sb.append("${filename}");
        sb.append(" ");

         sb.append(" -db ");
        sb.append(job.database);

        sb.append("   -num_threads 32   -max_target_seqs 10 -evalue 1E-09 ");

        sb.append(" -outfmt ");
        sb.append(Integer.toString(job.format.code));
         
        sb.append(" -out ");
        sb.append(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeOutputDirectory")   + "/" +  job.id);
        sb.append("/");
        sb.append("${base}");

        return sb.toString();
    }



    @Override
    public void run() {
        try {
            List<String> args = new ArrayList<>();
            BlastLaunchDTO job = getJob();
            BLASTProgram bp = job.program;
            String program = clusterProperties.getProperty("LocationOfLocalBLASTPrograms")  + job.program.toString().toLowerCase();
         String blastName = bp.toString().toLowerCase();
            if(OSValidator.isWindows()) {
                blastName =  "./" + program +  ".exe";
            }
            args.add(program);
            args.add("-query");
            String qpath = job.query.getAbsolutePath();
            args.add(job.query.getPath());
            args.add("-db");
            args.add(job.database.replace("-remote",""));
            args.add("-out");
            args.add(job.output);
            args.add("-outfmt");
            args.add(Integer.toString(job.format.code));



            for (String parameter : parameters.keySet()) {
                if(parameter.equalsIgnoreCase("email"))
                    continue;
                if(parameter.equalsIgnoreCase("user"))
                    continue;
                if(parameter.equalsIgnoreCase("JobId"))
                    continue;
                args.add("-" + parameter);
                String value = parameters.get(parameter).toString();
                args.add(value);
             }

            state.set(JobState.RunStarted);
            StringBuilder sb = new StringBuilder();
            String[] commandargs = args.toArray(new String[0]);
            for (int i = 0; i < commandargs.length; i++) {
                String commandarg = commandargs[i];
                System.out.println(commandarg + " ");
                sb.append(commandarg + " ");
            }
            System.out.println(sb.toString());
              String result = BLastTools.executeCommand( commandargs);
            state.set(JobState.JobFinished);
        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }

    }



    public static void main(String[] args) throws InterruptedException {
        Map<String, String> data = new HashMap<>();
        BlastLaunchDTO dto = handleLocBlastArgs(args);
        SlurmLocalRunner me = new SlurmLocalRunner(dto,data);
        Thread t = new Thread(me);
        t.start();
        t.join();
        System.out.println(me.job.id);
    }
}

