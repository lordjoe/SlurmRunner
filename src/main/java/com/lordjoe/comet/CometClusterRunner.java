package com.lordjoe.comet;

import com.jcraft.jsch.ChannelSftp;
import com.lordjoe.ssh.*;
import com.lordjoe.utilities.FileUtilities;
import com.lordjoe.utilities.ILogger;

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


    public CometClusterRunner(CometLaunchDTO job, Map<String, ? extends Object> param) {
        super(job, param);

    }

    public static String getSlurmAdded() {
        String ret = System.getProperty("slurm_added");
        if(ret == null)
            return "";
        return ret;
    }
    public String generateSlurmScript() {
        StringBuilder sb = new StringBuilder();
        int CPUsPerNode = Integer.parseInt(getClusterProperties().getProperty("CPUsPerNode"));

        sb.append("#! /bin/bash\n" +
                "#\n" +
                "### $1 is the input for blast file name with path\n" +
                "### $2 is the counter from the calling script, used for output\n" +
                "#\n" +
                "#SBATCH --ntasks=1\n" +
                "#SBATCH --cpus-per-task=" + CPUsPerNode + "\n" +
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
        String program = getClusterProperties().getProperty("LocationOfJava");
        sb.append(program);
        sb.append("java -jar");

        sb.append(" " + locationOfDefaultDirectory + "SLURM_Runner.jar ");
        sb.append(" com.lordjoe.comet.MergeCometXML ");


        sb.append(locationOfDefaultDirectory + getClusterProperties().getProperty("RelativeOutputDirectory") + "/" + job.id);
        sb.append("/ ");
        String output = getCometOutputName();
        //    String outputx = output.substring(Math.max(0, output.indexOf("/") + 1));
        sb.append(locationOfDefaultDirectory + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id + "/");
        sb.append(output);

        String s = sb.toString();
        System.out.println(s);
        return s;
    }


    public String getDatabaseFile(Properties clusterProperties) {
        String baseDir = clusterProperties.getProperty("LocationOfDefaultDirectory");
        String ret;
        if (job.isDatabaseIsRemote())
            ret = clusterProperties.getProperty("LocationOfCometDb")  + "/" + job.getJobDatabaseName();
        else
            ret = baseDir + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/" + job.getJobDatabaseName();
        return ret;
    }


    public String generateExecutionScript() {


        Properties clusterProperties = getClusterProperties();
        String baseDir = clusterProperties.getProperty("LocationOfDefaultDirectory");

    //    int CPUsPerNode = Integer.parseInt(getClusterProperties().getProperty("CPUsPerNode"));
        int numberThreads = Integer.parseInt(getClusterProperties().getProperty("ThreadsPerProcessor") );

        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n" +
                "#\n" +
                "### $1 is the input for blast file name with path\n" +
                "#\n" +
                "#SBATCH --ntasks=1\n" +
   //             "#SBATCH --cpus-per-task=" + CPUsPerNode + "\n" +
                "#SBATCH --output=batchOutput$2.txt\n");
        sb.append("filename=${1}\n");
        sb.append("base=`basename \"$filename\"`\n");
        sb.append("base1=${base%.*}\n");


        String program = getClusterProperties().getProperty("LocationOfComet");
        sb.append(program);
        sb.append(" -P");
        String name = job.getParams().getName().replace(" ","");
        sb.append(baseDir + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/" + name);
        sb.append(" -D");
        sb.append(getDatabaseFile(clusterProperties));
        sb.append(" -N");
        sb.append(baseDir + clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id + "/${base1}");
        sb.append("  ");
        sb.append(baseDir + clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id + "/${base}");


        String s = sb.toString();
        System.out.println(s);
        return s;
    }

    protected String makeSampleSubmitToCPU(String data) {
        data = data.replace("${base1}", "splitFile001");
        data = data.replace("${base}", "splitFile001.mgf");
        System.out.println(data);
        return data;
    }


    public String generateSlurmIterateScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("for file in ");
        sb.append(getClusterProperties().getProperty("LocationOfDefaultDirectory") + getClusterProperties().getProperty("RelativeInputDirectory") + "/" + job.id);
        sb.append("/*\n");

        sb.append("do\n");
        sb.append("filename=${file}\n");

        String slurmAdded = SlurmClusterRunner.getSlurmAdded();

        String jobScript = getClusterProperties().getProperty("LocationOfDefaultDirectory") + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id + "/submitToCPUNode.sh";
        sb.append(" sbatch --time=3:00:00 " + slurmAdded + "--job-name=" +  job.id + "$fileName " + jobScript + " $file   \n");
        sb.append("done\n");

        return sb.toString();
    }


    /**
     * comput the file to upload which may be zipped from the job
     *
     * @param job
     * @return
     */
    public String getClusterMergeResultZipFileName(LaunchDTO job) {
        //     ChannelSftp sftp = session.getSFTP();
        String outputx = getCometOutputZipFileName();
        return outputx;
    }


    public void writeExecutionScript(ClusterSession me) {
        try {

            String file = "submitToCPUNode.sh";
            String data = generateExecutionScript();
            InputStream is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is, 0777);

            file = "sampleSubmitToCPUNode.sh";
            data = makeSampleSubmitToCPU(data);
            is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is, 0777);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeMergerScript(ClusterSession me) {
        try {
            String file = "mergeXMLFiles.sh";
            String data = generateMergerScript();
            InputStream is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is, 0777);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeIterationScript(ClusterSession me) {
        try {
            String file = "runBlast.sh";
            String data = generateSlurmIterateScript();
            InputStream is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is, 0777);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Integer> getJobNumbers(ClusterSession me) {
        SSHUserData user1 = getUser();
        return getJobNumbers(me, user1.userName);
    }

    public static Set<Integer> getJobNumbers(ClusterSession me, String user) {
        try {
            Set<Integer> ret = new HashSet<>();
            System.out.println("Testing Job Queue");
            String answer = me.executeOneLineCommand("squeue -u " + user);
            String[] items = answer.split("\n");
            for (String item : items) {
                if (item.contains(user)) {
                    Integer e = parseJobId(item);
                    if (e > 0)
                        ret.add(e);
                }
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void waitEmptyJobQueue(ClusterSession me, Set<Integer> priors) {
        int sleepTime = 3000;
        int maxSleepTime = 240000;
        justSleep(10000); // make sure we have jobs
        SSHUserData user1 =  getUser();
        String user = user1.userName;
        Set<Integer> current;
        while (true) {
            current = getJobNumbers(me, user);
            current.removeAll(priors);
            if (current.isEmpty())
                return;
            justSleep(sleepTime);
            sleepTime *= 2;
            sleepTime = Math.min(sleepTime,maxSleepTime);
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
            ClusterSession me =  getClusterSession();
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
            String path = directoryOnCluster;
            me.mkdir(path);
            //        me.executeCommand("chmod a+rwx " + path);


            writeExecutionScript(me);
            writeMergerScript(me);
            writeIterationScript(me);


            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

             releaseClusterSession(me);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public File splitSpectra(File in) {
        int numberEntries = countMGFEntities(in);

        int numberProcessors = ClusterLauncher.getNumberProcessors();

        int splitSize = numberEntries;
        if (numberEntries > 70)
            splitSize = (numberEntries / numberProcessors);

        File outDirectory = new File(getClusterProperties().getProperty("RelativeInputDirectory") + "/" + job.id);
        outDirectory.mkdirs();
        outDirectory.setReadable(true, true);

        String baseName = "splitFile";
        splitMGFFile(in, outDirectory, baseName, splitSize, numberEntries);
        return outDirectory;
    }


    public void transferFilesToCluster() {
        try {
            File outDirectory = new File(getClusterProperties().getProperty("RelativeInputDirectory") + "/" + job.id);
            File[] files = outDirectory.listFiles();
            if (files != null) {
                String directoryOnCluster = getClusterProperties().getProperty("LocationOfDefaultDirectory") +
                        getClusterProperties().getProperty("RelativeInputDirectory") + "/" + job.id;
                ClusterSession me =  getClusterSession();
                me.cd(directoryOnCluster);
                //               me.mkdir(directoryOnCluster);

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
                    System.out.println("Uploading " + file.getAbsolutePath());
                    me.ftpFileCreate(path, is, 0777);
                    System.out.println("Uploaded " + file.getAbsolutePath());
                }
                // cleanup local copy
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
                outDirectory.delete();

                directoryOnCluster = getClusterProperties().getProperty("LocationOfDefaultDirectory") +
                        getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id;

                // copy spectrum file
                FileInputStream is = new FileInputStream(job.getSpectra());
                String fileName = job.getSpectra().getName();
                String path = directoryOnCluster + "/" + fileName;
                //           me.prepareUpload(me.getSFTP(),path,true);
                me.ftpFileCreate(path, is, 0777);
                System.out.println("Uploaded " + job.getSpectra().getAbsolutePath());

                // copy fasta file
                if (!job.isDatabaseIsRemote()) {
                    File file = new File("/opt/blastserver/" + /* getId() + "/" + */ job.getJobDatabaseName());
                    is = new FileInputStream(file);
                    fileName = file.getName();
                    path = directoryOnCluster + "/" + fileName;
                    //           me.prepareUpload(me.getSFTP(),path,true);
                    me.ftpFileCreate(path, is, 0777);
                    System.out.println("Uploaded " + fileName);
                }

                // copy params file
                File params = job.getParams();
                is = new FileInputStream(params);
                path = directoryOnCluster + "/" + params.getName();
                //           me.prepareUpload(me.getSFTP(),path,true);
                me.ftpFileCreate(path, is, 0777);
                System.out.println("Uploaded " + params.getAbsolutePath());


                releaseClusterSession(me);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    protected void cleanUp() {
        String directoryOnCluster = getClusterProperties().getProperty("LocationOfDefaultDirectory") +
                getClusterProperties().getProperty("RelativeInputDirectory") + "/" + job.id;
        ClusterSession me =  getClusterSession();
        ChannelSftp sftp = me.getSFTP();
        ClusterSession.recursiveFolderDelete(sftp, directoryOnCluster);
        directoryOnCluster = getClusterProperties().getProperty("LocationOfDefaultDirectory") +
                getClusterProperties().getProperty("RelativeOutputDirectory") + "/" + job.id;
        ClusterSession.recursiveFolderDelete(sftp, directoryOnCluster);
        directoryOnCluster = getClusterProperties().getProperty("LocationOfDefaultDirectory") +
                getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id;
        ClusterSession.recursiveFolderDelete(sftp, directoryOnCluster);

         releaseClusterSession(me);
    }


    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            String defaultDirectory = getClusterProperties().getProperty("LocationOfDefaultDirectory");


            logMessage("guaranteeJarFile");
            guaranteeJarFile(0);


            ClusterSession session =  getClusterSession();

            if (!session.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");
            logMessage("Directory Guaranteed");
            setState(JobState.JarGuaranteed);
             releaseClusterSession(session);

            writeScripts();
            logMessage("writeScripts");

            File spectra = job.getSpectra();
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

            String command = defaultDirectory + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id + "/" + "runBlast.sh";
            setState(JobState.BlastCalled);

            session.executeOneLineCommand(command);
            waitEmptyJobQueue(session, priors);
            logMessage("blast run");
            setState(JobState.BlastFinished);

            // what is the user running before we start
            priors = getJobNumbers(session);

            String relativeScriptDirectory = getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id;

            String slurmAdded = SlurmClusterRunner.getSlurmAdded();
            command = "salloc  --time=1:00:00 " + slurmAdded + defaultDirectory + relativeScriptDirectory + "/" + "mergeXMLFiles.sh";
            System.out.println(command);
            session.executeOneLineCommand(command);

            waitEmptyJobQueue(session, priors);
            logMessage("files merged");
            setState(JobState.FilesMerged);


            String outputx = defaultDirectory + relativeScriptDirectory + "/" + getClusterMergeResultZipFileName(job);    // use the Upload file name to find the file to upload
            System.out.println(outputx);
            File img = new File(outputx);
            File localJobDirectory = job.getLocalJobDirectory();
            String name = img.getName();
            File outfile = new File(localJobDirectory, name);
            session.ftpFileGet(outfile, outputx);
            setState(JobState.OututDownloaded);
            FileUtilities.setReadWritePermissions(outfile);


            //     cleanUp();
            setState(JobState.FilesCleanedUp);

            ILogger logger = getLogger();
            sendEmail(logger);
            setState(JobState.NotificationSent);
             releaseClusterSession(session);

            setState(JobState.JobFinished);
            logMessage(job.id + " is completed");
            this.logger.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            logMessage(getStaceTraceString(e));
            setState(JobState.Failed);
        }
        long runTime =  (int)((System.currentTimeMillis() - startTime) / 60000);
        System.out.println("Ran in "  + runTime + "Min");
         logMessage("Ran in "  + runTime + "Min");
        System.exit(0);
    }

    public static CometClusterRunner run(Map<String, String> data) {
        ClusterSession.fixLogging();
        CometLaunchDTO dto = handleLocBlastArgs(data);
        CometClusterRunner me = new CometClusterRunner(dto, data);
        me.logMessage("Starting SlurmClusterRunner");
        me.logMessage(" id " + dto.id);
        me.logMessage(" query " + dto.getSpectra().getAbsolutePath());
        me.logMessage(" db " + dto.getJobDatabaseName());
        me.logMessage(" out " + new File(dto.getLocalJobDirectory(), dto.getOutputZipFileName()));

        new Thread(me).start();

        return me;
    }

    public static void run(String[] args) {
        Map<String, ?> data = buildParameters(args);
        ClusterSession.fixLogging();
        CometLaunchDTO dto = handleLocBlastArgs(args);
        String user = (String)data.get("user");
        String email = (String)data.get("email");
        SSHUserData user1 = SSHUserData.getUser(email);
        CometClusterRunner me = new CometClusterRunner(dto, data);

        me.run();

    }

    public static final boolean RUN_LOCAL = false;

    public static void main(String[] args) {
        System.setProperty("slurm_added"," --account=p200006 --qos=default ");
        if (RUN_LOCAL)
            CometLocalRunner.run(args);
        else
            CometClusterRunner.run(args);
    }
}




