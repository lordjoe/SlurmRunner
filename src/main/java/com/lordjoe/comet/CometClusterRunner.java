package com.lordjoe.comet;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.lordjoe.locblast.BlastLaunchDTO;
import com.lordjoe.ssh.ClusterSession;
import com.lordjoe.ssh.JobState;
import com.lordjoe.utilities.FileUtilities;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * com.lordjoe.ssh.SlurmClusterRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 */
public class CometClusterRunner extends AbstractCometClusterRunner {

    static Logger LOG = Logger.getLogger(CometClusterRunner.class.getName());



    public CometClusterRunner(BlastLaunchDTO job, Map<String, ? extends Object> param) {
        super(job, param);

    }

 


    public String generateSlurmScript() {
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
        sb.append("srun  -n1 --exclusive ");
        sb.append(generateExecutionScript());
        sb.append("\n");
        sb.append("wait\n");
        return sb.toString();
    }


    public String generateMergerScript() {
        String locationOfDefaultDirectory = getClusterProperties().getProperty("LocationOfDefaultDirectory");

        StringBuilder sb = new StringBuilder();
        sb.append("java -jar");

        sb.append(" " + locationOfDefaultDirectory + "SLURM_Runner.jar ");
               sb.append(" com.lordjoe.comet.MergeCometXML ") ;


        sb.append(locationOfDefaultDirectory + getClusterProperties().getProperty("RelativeOutputDirectory") + "/" + job.id);
        sb.append("/ ");
        String output = getOutputName();
    //    String outputx = output.substring(Math.max(0, output.indexOf("/") + 1));
        sb.append(locationOfDefaultDirectory + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id  + "/");
        sb.append(output);

         String s = sb.toString();
        System.out.println(s);
        return s;
    }




    public String generateExecutionScript() {


        Properties clusterProperties = getClusterProperties();
        String baseDir = clusterProperties.getProperty("LocationOfDefaultDirectory");


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
  


        String program = "comet ";
        sb.append(program);
        sb.append(" -P");
        sb.append(baseDir + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/"  + job.output);
        sb.append(" -D");
        sb.append(baseDir + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/"  + job.query.getName());
        sb.append(" -N");
        sb.append(baseDir + clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id   + "/${base1}");
        sb.append("  ");
        sb.append(baseDir + clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id   + "/${base}");




        String s = sb.toString();
        System.out.println(s);
        return s;
    }
    protected String makeSampleSubmitToCPU(String data) {
        Properties clusterProperties = getClusterProperties();
        String baseDir = clusterProperties.getProperty("LocationOfDefaultDirectory");
        StringBuilder sb = new StringBuilder();
        String base1 = "splitFile001";
        String basef = "splitFile001.mgf";

        String program = "comet ";
        sb.append(program);
        sb.append(" -P");
        sb.append(baseDir + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/"  + job.output);
        sb.append(" -D");
        sb.append(baseDir + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/"  + job.query.getName());
        sb.append(" -N");
        sb.append(baseDir + clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id + "/"   + "/" + base1);
        sb.append("  ");
        sb.append(baseDir + clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id + "/"   + "/" + basef);




        String s = sb.toString();
        System.out.println(s);
        return s;
    }


    public String generateSlurmIterateScript() {
        StringBuilder sb = new StringBuilder();
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


    public void writeExecutionScript(ClusterSession me) {
        try {

            String file = "submitToCPUNode.sh";
            String data = generateExecutionScript();
            InputStream is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is,0777);

            file = "sampleSubmitToCPUNode.sh";
            data = makeSampleSubmitToCPU(data);
            is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is,0777);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public void writeMergerScript(ClusterSession me) {
        try {
            String file = "mergeXMLFiles.sh";
            String data = generateMergerScript();
            InputStream is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is,0777);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeIterationScript(ClusterSession me) {
        try {
            String file = "runBlast.sh";
            String data = generateSlurmIterateScript();
            InputStream is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is,0777);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public   Set<Integer> getJobNumbers(ClusterSession me) {
        String user = getClusterProperties().getProperty("UserName");
        return getJobNumbers(me, user);
    }

    public static Set<Integer> getJobNumbers(ClusterSession me, String user) {
        try {
            Set<Integer> ret = new HashSet<>();
            String answer = me.executeOneLineCommand("squeue -u " + user);
            String[] items = answer.split("\n");
            for (String item : items) {
                if (item.contains(user)) {
                    ret.add(parseJobId(item));
                }
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void waitEmptyJobQueue(ClusterSession me, Set<Integer> priors) {
        justSleep(2000); // make sure we have jobs
        String user = getClusterProperties().getProperty("UserName");
        Set<Integer> current;
        while (true) {
            current = getJobNumbers(me, user);
            current.removeAll(priors);
            if (current.isEmpty())
                return;
            justSleep(1000);
        }

    }

    public static void justSleep(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException e) {
        }
    }

    public void cdDefaultDirectory(ClusterSession me) {

    }

    public void writeScripts() {
        try {
            String defaultDirectory = getClusterProperties().getProperty("LocationOfDefaultDirectory");
            ClusterSession me = ClusterSession.getClusterSession();
            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

            String outputDirectoryOnCluster = getClusterProperties().getProperty("RelativeOutputDirectory") + "/" + job.id;
            me.mkdir(outputDirectoryOnCluster);
     //       me.executeCommand("chmod a+rwx " + defaultDirectory + outputDirectoryOnCluster);

                if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

            String directoryOnCluster = getClusterProperties().getProperty("RelativeInputDirectory") + "/" + job.id;
            me.mkdir(directoryOnCluster);
 //           me.executeCommand("chmod a+rwx " + defaultDirectory + directoryOnCluster);


            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");
            directoryOnCluster = getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id;
            String path =   directoryOnCluster;
            me.mkdir(path);
    //        me.executeCommand("chmod a+rwx " + path);


            writeExecutionScript(me);
            writeMergerScript(me);
            writeIterationScript(me);


            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

            String ScriptJobDir = defaultDirectory + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id;
            ChannelSftp sftp = me.getSFTP();
            sftp.chmod(0777,ScriptJobDir);
            me.executeOneLineCommand("chmod a+x " + defaultDirectory + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id + "/*");
             ClusterSession.releaseClusterSession(me);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public File splitSpectra(File in) {
        int numberEntries = countMGFEntities(in);

        int splitSize = numberEntries;
        if (numberEntries > 70)
            splitSize = (numberEntries / 7);

        File outDirectory = new File(getClusterProperties() .getProperty("RelativeInputDirectory") + "/" + job.id);
        outDirectory.mkdirs();
        outDirectory.setReadable(true,true);

        String baseName = "splitFile";
        splitMGFFile(in, outDirectory, baseName, splitSize, numberEntries);
        return outDirectory;
    }


    public void transferFilesToCluster() {
        try {
            File outDirectory = new File(getClusterProperties() .getProperty("RelativeInputDirectory") + "/" + job.id);
            File[] files = outDirectory.listFiles();
            if (files != null) {
                String directoryOnCluster = getClusterProperties() .getProperty("LocationOfDefaultDirectory") +
                        getClusterProperties() .getProperty("RelativeInputDirectory") + "/" + job.id;
                ClusterSession me = ClusterSession.getClusterSession();
                me.mkdir(directoryOnCluster);

//                try {
//                    ChannelSftp sftp = me.getSFTP();
//                    sftp.cd(getClusterProperties() .getProperty("RelativeInputDirectory"));
//                    sftp.cd(job.id);
//                } catch (SftpException e) {
//                    throw new RuntimeException(e);
//                }

                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    FileInputStream is = new FileInputStream(file);
                    //              me.ftpFileCreate(directoryOnCluster + "/" + file.getName(),is);
                    String fileName = file.getName();
                    String path = directoryOnCluster + "/" + fileName;
                    //           me.prepareUpload(me.getSFTP(),path,true);
                    me.ftpFileCreate(path, is,777);
                    System.out.println("Uploaded " + file.getAbsolutePath());
                }
                // cleanup local copy
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
                outDirectory.delete();

                directoryOnCluster = getClusterProperties() .getProperty("LocationOfDefaultDirectory") +
                        getClusterProperties() .getProperty("RelativeScriptDirectory") + "/" + job.id;

                // copy fasta file
                FileInputStream is = new FileInputStream(job.query);
                String fileName = job.query.getName();
                String path = directoryOnCluster + "/" + fileName;
                //           me.prepareUpload(me.getSFTP(),path,true);
                me.ftpFileCreate(path, is,0777);
                System.out.println("Uploaded " + job.query.getAbsolutePath());

                // copy params file
                File params = new File(job.query.getParentFile(),job.output);
                 is = new FileInputStream(params);

                 path = directoryOnCluster + "/" + job.output;
                //           me.prepareUpload(me.getSFTP(),path,true);
                me.ftpFileCreate(path, is,0777);
                System.out.println("Uploaded " + params.getAbsolutePath());


                ClusterSession.releaseClusterSession(me);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    protected void cleanUp() {
        String directoryOnCluster = getClusterProperties() .getProperty("LocationOfDefaultDirectory") +
                getClusterProperties() .getProperty("RelativeInputDirectory") + "/" + job.id;
        ClusterSession me = ClusterSession.getClusterSession();
        ChannelSftp sftp = me.getSFTP();
        ClusterSession.recursiveFolderDelete(sftp, directoryOnCluster);
        directoryOnCluster = getClusterProperties() .getProperty("LocationOfDefaultDirectory") +
                getClusterProperties() .getProperty("RelativeOutputDirectory") + "/" + job.id;
        ClusterSession.recursiveFolderDelete(sftp, directoryOnCluster);
        directoryOnCluster = getClusterProperties() .getProperty("LocationOfDefaultDirectory") +
                getClusterProperties() .getProperty("RelativeScriptDirectory") + "/" + job.id;
        ClusterSession.recursiveFolderDelete(sftp, directoryOnCluster);

        ClusterSession.releaseClusterSession(me);
    }




 


    @Override
    public void run() {
        try {
            String defaultDirectory = getClusterProperties() .getProperty("LocationOfDefaultDirectory");


            logMessage("guaranteeJarFile");
            guaranteeJarFile(0);


            ClusterSession session = ClusterSession.getClusterSession();

            if (!session.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");
            logMessage("Directory Guaranteed");
            setState(JobState.JarGuaranteed);
            ClusterSession.releaseClusterSession(session);

            writeScripts();
            logMessage("writeScripts");

            File base = job.query.getParentFile();
            File spectra = new File(base,job.database);
            String path = spectra.getAbsolutePath();
            File outDirectory = splitSpectra(spectra);
            setState(JobState.ScriptsWritten);

            transferFilesToCluster();


            setState(JobState.InputUploaded);
            logMessage("transferFilesToCluster");

            logMessage("Clean up directory " + outDirectory.getAbsolutePath());
            FileUtilities.expungeDirectory(outDirectory);
            logMessage("clean split");

            // what is the user running before we start
            Set<Integer> priors = getJobNumbers(session);

            String command = defaultDirectory + getClusterProperties() .getProperty("RelativeScriptDirectory") + "/" + job.id + "/" + "runBlast.sh";
            setState(JobState.BlastCalled);

            session.executeOneLineCommand(command);
            waitEmptyJobQueue(session, priors);
            logMessage("blast run");
            setState(JobState.BlastFinished);

            // what is the user running before we start
            priors = getJobNumbers(session);

            command = "salloc " + defaultDirectory + getClusterProperties() .getProperty("RelativeScriptDirectory") + "/" + job.id + "/" + "mergeXMLFiles.sh";
            System.out.println(command);
            session.executeOneLineCommand(command);

            waitEmptyJobQueue(session, priors);
            logMessage("files merged");
            setState(JobState.FilesMerged);


            //     ChannelSftp sftp = session.getSFTP();
              String outputx = getOutputName();
            StringBuilder sb = new StringBuilder();

            sb.append(getClusterProperties() .getProperty("LocationOfDefaultDirectory") + getClusterProperties() .getProperty("RelativeScriptDirectory") + "/" + job.id + "/");
            sb.append(outputx);


            String mergedOutput = sb.toString();

            File outfile = new File(job.getLocalJobDirectory(),outputx);
            session.ftpFileGet(outfile, mergedOutput);
            setState(JobState.OututDownloaded);


            //     cleanUp();
            setState(JobState.FilesCleanedUp);

            sendEmail(getLogger());
            setState(JobState.NotificationSent);
            ClusterSession.releaseClusterSession(session);

            setState(JobState.JobFinished);
            logMessage(job.id + " is completed");
            logger.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            logMessage(getStaceTraceString(e));
            setState(JobState.Failed);
        }
    }

    public static CometClusterRunner run(Map<String, String> data) {
        ClusterSession.fixLogging();
        BlastLaunchDTO dto = handleLocBlastArgs(data);
        CometClusterRunner me = new CometClusterRunner(dto, data);
        me.logMessage("Starting SlurmClusterRunner");
        me.logMessage(" id " + dto.id);
        me.logMessage(" query " + dto.query.getAbsolutePath());
        me.logMessage(" db " + dto.database);
        me.logMessage(" out " + new File(dto.getLocalJobDirectory(), dto.output));

        new Thread(me).start();

        return me;
    }

    public static void run(String[] args) {
        Map<String, ?> data = buildParameters(args);
        ClusterSession.fixLogging();
        BlastLaunchDTO dto = handleLocBlastArgs(args);
        CometClusterRunner me = new CometClusterRunner(dto, data);

        me.run();

    }

     public static final boolean  RUN_LOCAL = false;
    public static void main(String[] args) {
        if(RUN_LOCAL)
            LocalCometRunner.run(args);
        else
            CometClusterRunner.run(args);
    }
}




