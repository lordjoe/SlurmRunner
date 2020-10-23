/*
package com.lordjoe.comet;


import com.lordjoe.locblast.BlastLaunchDTO;
import com.lordjoe.ssh.JobState;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

*/
/**
 * com.lordjoe.ssh.SlurmClusterRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 *//*

public class CometLocalRunner extends AbstractCometClusterRunner {

    static Logger LOG = Logger.getLogger(CometLocalRunner.class.getName());


    public CometLocalRunner(BlastLaunchDTO job, Map<String, ? extends Object> param) {
        super(job, param);

    }


    public String generateExecutionScript() {
        Set<String> usedParameters = new HashSet<>();
        // never use these
        usedParameters.add("email");
        usedParameters.add("user");
        usedParameters.add("JobId");
        // har code these
        usedParameters.add("query");
        usedParameters.add("out");
        usedParameters.add("db");
        usedParameters.add("outfmt");
        usedParameters.add("num_threads");


        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n" +
                "#\n" +
                "### $1 is the input for blast file name with path\n" +
                "#\n" +
                "#SBATCH --ntasks=1\n" +
                "#SBATCH --cpus-per-task=32\n" +
                "#SBATCH --output=batchOutput$2.txt\n");
        sb.append("filename=${1}\n");
        sb.append("base=`basename \"$filename\"`\n");
        sb.append("base1=${base%.*}\n");
        sb.append("base=${base1}.pep.xml\n");


        sb.append("export BLASTDB=" + getClusterProperties().getProperty("LocationOfDatabaseFiles") + "\n");

        String program = getClusterProperties().getProperty("LocationOfBLASTPrograms") + job.program.toString().toLowerCase();
        sb.append(program);
        sb.append(" -query ");

        sb.append("${filename}");
        sb.append(" ");

        sb.append(" -db ");
        // it does not liek -remote
        sb.append(job.database.replace("-remote", ""));

        sb.append("   -num_threads 32   ");


        sb.append(" -out ");
        String str = getClusterProperties().getProperty("LocationOfDefaultDirectory") + getClusterProperties().getProperty("RelativeOutputDirectory") + "/" + job.id;
        str += "/" + "${base}";
        sb.append(str);


        for (String parameter : parameters.keySet()) {
            if (usedParameters.contains(parameter))
                continue;
            usedParameters.add(parameter);
            sb.append(" -" + parameter);
            String value = parameters.get(parameter).toString();
            sb.append(" ");
            sb.append(value);
        }


        String s = sb.toString();
        System.out.println(s);
        return s;
    }


    public String generateSlurmIterateScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("export BLASTDB=" + getClusterProperties().getProperty("LocationOfDatabaseFiles") + "\n");
        sb.append("for file in ");
        sb.append(getClusterProperties().getProperty("LocationOfDefaultDirectory") + getClusterProperties().getProperty("RelativeInputDirectory") + "/" + job.id);
        sb.append("/*\n");

        sb.append("do\n");
        sb.append("filename=${file}\n");
        String jobScript = getClusterProperties().getProperty("LocationOfDefaultDirectory") + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id + "/submitToCPUNode.sh";
        sb.append(" sbatch --job-name=" + job.id + "$fileName " + jobScript + " $file   \n");
        sb.append("done\n");

        return sb.toString();
    }


    protected void cleanUp() {
    }



    protected void copyDatabase(File directory) {
        File db = new File("/opt/blastserver/" + job.database); // todo improve
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(db));
            PrintWriter out = new PrintWriter(new FileWriter(new File(directory,job.database)));
            String line = rdr.readLine();
            while(line != null)  {
                out.println(line);
                line = rdr.readLine();
            }
            out.close();
            rdr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    protected void copyParams(File directory) {
        File db = new File("/opt/blastserver/" + job.output); // todo improve
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(db));
            PrintWriter out = new PrintWriter(new FileWriter(new File(directory,job.output)));
            String line = rdr.readLine();
            while(line != null)  {
                out.println(line);
                line = rdr.readLine();
            }
            out.close();
            rdr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    protected File getParamsFile() {
        return new File(job.output) ;

    }

    public List<String> readFileLines(File f) {
        try {
            List<String> lines = new ArrayList<>();
            LineNumberReader rdr = new LineNumberReader(new FileReader(f));
            String line = rdr.readLine();
            while(line != null)  {
                lines.add(line);
                line = rdr.readLine();
            }
            return lines;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
    public void writeFileLines(File f,List<String> lines) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(f)) ;
              for (String line : lines) {
                  out.println(line);
               }
             out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public void run() {
        String defaultDirectory = getClusterProperties().getProperty("LocationOfDefaultDirectory");

        File jobDirectory = new File("/opt/blastserver/" + job.id);
        jobDirectory.mkdirs();
        File scriptsDirDirectory = new File(jobDirectory,"scripts");
        scriptsDirDirectory.mkdirs();


        copyParams(scriptsDirDirectory);

        logMessage("paramsCopied");


        setState(JobState.JarGuaranteed);

        copyDatabase(jobDirectory);

        File spectra = job.query;

        File inDirectory = new File(jobDirectory, "Input_Dir");

        inDirectory = splitSpectra(spectra,inDirectory);
        Runtime runtime = Runtime.getRuntime();

        File outDirectory = new File(jobDirectory, "Output_Dir");
        outDirectory.mkdirs();

        String commetCall = generateCometCall(outDirectory) ;
        File[] files = inDirectory.listFiles();
        if(files != null)  {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String ifn = file.getName();
                String ofbase = ifn.substring(0,ifn.lastIndexOf("."));
                String call = commetCall.replace("xyzzy",file.getName()) .replace("xyzzo",ofbase);
                System.out.println(call);
                try {
                    Process exec = runtime.exec(call);
                    int result = exec.waitFor();
                } catch (Exception e) {
                    throw new RuntimeException(e);

                }
            }
        }

        setState(JobState.BlastCalled);

        logMessage("blast run");
        setState(JobState.BlastFinished);

        logMessage("files merged");
        setState(JobState.FilesMerged);


        //     ChannelSftp sftp = session.getSFTP();
        String output = job.output.toString().replace("\\", "/");
        String outputx = output.substring(Math.max(0, output.indexOf("/")));
        StringBuilder sb = new StringBuilder();

        sb.append(getClusterProperties().getProperty("LocationOfDefaultDirectory") + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id + "/");
        sb.append(outputx);


        String mergedOutput = sb.toString();

        File outfile = new File(job.getLocalJobDirectory(), job.output);


        //     cleanUp();
        setState(JobState.FilesCleanedUp);

        sendEmail(getLogger());
        setState(JobState.NotificationSent);
        setState(JobState.JobFinished);
        logMessage(job.id + " is completed");
        logger.close();

    }

    private String generateCometCall(File outDirectory) {
        String baseDirectory = "/opt/blastserver/" + job.id ;
        StringBuilder sb = new StringBuilder();
        sb.append("comet ");
        sb.append(" -N" + baseDirectory + "/"  + "Output_Dir/xyzzo");
        sb.append(" -P" + baseDirectory + "/scripts/" +  job.output);
        sb.append(" -D" + baseDirectory + "/"  + job.database);
        sb.append("  " + baseDirectory + "/"  + "Input_Dir/xyzzy");
        return sb.toString();
    }

    public void OpenLogFile() throws IOException {
        File base = new File("/opt/blastserver");
        File jobdir = new File(base, job.id);
        jobdir.mkdirs();
        jobdir.setReadable(true, true);
        File logFile = new File(jobdir, "log.txt");
        FileWriter writer = new FileWriter(logFile, true); // append
        logger = new PrintWriter(writer);
    }


    public static CometLocalRunner run(Map<String, String> data) {
        BlastLaunchDTO dto = handleLocBlastArgs(data);
        CometLocalRunner me = new CometLocalRunner(dto, data);
        me.logMessage("Starting SlurmClusterRunner");
        me.logMessage(" id " + dto.id);
        me.logMessage(" query " + dto.query.getAbsolutePath());
        me.logMessage(" db " + dto.database);
        me.logMessage(" out " + new File(dto.getLocalJobDirectory(), dto.output));

        new Thread(me).start();

        return me;
    }

    public static CometLocalRunner run(String[] args) {
        Map<String, ?> data = buildParameters(args);
        BlastLaunchDTO dto = handleLocBlastArgs(args);
        CometLocalRunner me = new CometLocalRunner(dto, data);


        me.run();

        return me;
    }


    public static void main(String[] args) {
        CometLocalRunner.run(args);
    }
}




*/
