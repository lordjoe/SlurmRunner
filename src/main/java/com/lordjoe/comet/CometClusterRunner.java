package com.lordjoe.comet;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.lordjoe.fasta.FastaTools;
import com.lordjoe.ssh.BlastLaunchDTO;
import com.lordjoe.ssh.ClusterSession;
import com.lordjoe.ssh.JobState;
import com.lordjoe.utilities.FileUtilities;

import java.io.*;
import java.util.*;
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

    @Override

    public Properties buildClusterProperties() {
        try {
            Properties ret = new Properties();
            ret.load(new FileInputStream("ClusterLaunch.properties"));
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
         }
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
               sb.append(" com.lordjoe.blast.MergeCometXML ") ;


        sb.append(locationOfDefaultDirectory + getClusterProperties().getProperty("RelativeOutputDirectory") + "/" + job.id);
        sb.append("/ ");
        String output = job.output.toString().replace("\\", "/");
    //    String outputx = output.substring(Math.max(0, output.indexOf("/") + 1));
        sb.append(locationOfDefaultDirectory + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id + "/");
        sb.append(output);

        String s = sb.toString();
        return s;
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

        sb.append(" -outfmt ");
        sb.append(Integer.toString(job.format.code));

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


    public void writeExecutionScript(ClusterSession me) {
        try {

            String file = "submitToCPUNode.sh";
            String data = generateExecutionScript();
            InputStream is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is);

            file = "sampleSubmitToCPUNode.sh";
            data = makeSampleSubmitToCPU(data);
            is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String makeSampleSubmitToCPU(String data) {
        String inputLocationDir = getClusterProperties().getProperty("LocationOfDefaultDirectory") + getClusterProperties().getProperty("RelativeInputDirectory") + "/" + job.id;
        String inputLocation = inputLocationDir + "/splitFile001.faa";
        data = data.replace("${filename}", inputLocation);
             data = data.replace("${base}", "splitFile001.xml");

        data = data.replace("batchOutput$2.txt", "batchOutput$2.txt\n#SBATCH --output=" + inputLocationDir + "/error.txt");
        return data;
    }


    public void writeMergerScript(ClusterSession me) {
        try {
            String file = "mergeXMLFiles.sh";
            String data = generateMergerScript();
            InputStream is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeIterationScript(ClusterSession me) {
        try {
            String file = "runBlast.sh";
            String data = generateSlurmIterateScript();
            InputStream is = new ByteArrayInputStream(data.getBytes());
            me.ftpFileCreate(file, is);
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

    protected static Integer parseJobId(String item) {
        item = item.trim();
        int index = item.indexOf(" ");
        String s = item.substring(0, index).trim();
        return new Integer(s);
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


            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

            String directoryOnCluster = getClusterProperties().getProperty("RelativeInputDirectory") + "/" + job.id;
            me.mkdir(directoryOnCluster);


            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");
            directoryOnCluster = getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id;
            me.mkdir(directoryOnCluster);


            writeExecutionScript(me);
            writeMergerScript(me);
            writeIterationScript(me);


            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

            me.executeOneLineCommand("chmod a+x " + defaultDirectory + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id + "/*");
            me.executeOneLineCommand("chmod a+rw " + defaultDirectory + getClusterProperties().getProperty("RelativeScriptDirectory") + "/" + job.id);
            ClusterSession.releaseClusterSession(me);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File splitQuery(File in) {
        int numberEntries = FastaTools.countFastaEntities(in);

        int splitSize = numberEntries;
        if (numberEntries > 70)
            splitSize = (numberEntries / 7);

        File outDirectory = new File(getClusterProperties() .getProperty("RelativeInputDirectory") + "/" + job.id);
        outDirectory.mkdirs();

        String baseName = "splitFile";
        FastaTools.splitFastaFile(in, outDirectory, baseName, splitSize, numberEntries);
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
                    me.ftpFileCreate(path, is);
                }
                // cleanup local copy
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
                outDirectory.delete();
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

    protected void guaranteeJarFile(int call) {
        File defaultDir = new File("/opt/blastserver");
        File local = new File(defaultDir, "SLURM_Runner.jar");
        if (!local.exists()) {
            String path = local.getAbsolutePath();
            throw new IllegalStateException("local jar not found at " + path);
        }
        long size = local.length();
        logMessage("LocalFileFound");
        try {
            String remoteFile = getClusterProperties() .getProperty("LocationOfDefaultDirectory") + "SLURM_Runner.jar";
            ClusterSession me = ClusterSession.getClusterSession();
            ChannelSftp sftp = me.getSFTP();
            SftpATTRS fileStat = null;
            try {
                fileStat = sftp.lstat(remoteFile);
                long remotesize = fileStat.getSize();
                if (remotesize == size) {
                    ClusterSession.releaseClusterSession(me);
                    logMessage("Remote Jar Same");
                    return;
                }
            } catch (SftpException e) {
                FileInputStream is = new FileInputStream(local);
                me.ftpFileCreate(remoteFile, is);
                logMessage("Remote Jar Downloaded");
                if (call == 0)
                    guaranteeJarFile(call + 1);
            }
            ClusterSession.releaseClusterSession(me);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }


    }



    protected String buildDownloadUrl() {
        StringBuilder sb = new StringBuilder();
        String tomcatURL = getClusterProperties() .getProperty("TomcatUrl");
        sb.append(tomcatURL);
        sb.append("?filename=");
        sb.append(job.output);
        sb.append("&directory=");
        sb.append(job.id);

        return sb.toString();
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
            File outDirectory = splitQuery(job.query);
            writeScripts();
            logMessage("writeScripts");
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
            session.executeOneLineCommand(command);

            waitEmptyJobQueue(session, priors);
            logMessage("files merged");
            setState(JobState.FilesMerged);


            //     ChannelSftp sftp = session.getSFTP();
            String output = job.output.toString().replace("\\", "/");
            String outputx = output.substring(Math.max(0, output.indexOf("/")));
            StringBuilder sb = new StringBuilder();

            sb.append(getClusterProperties() .getProperty("LocationOfDefaultDirectory") + getClusterProperties() .getProperty("RelativeScriptDirectory") + "/" + job.id + "/");
            sb.append(outputx);


            String mergedOutput = sb.toString();

            File outfile = new File(job.getLocalJobDirectory(), job.output);
            session.ftpFileGet(outfile, mergedOutput);
            setState(JobState.OututDownloaded);


            //     cleanUp();
            setState(JobState.FilesCleanedUp);

            sendEmail();
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

    public static String getStaceTraceString(Exception e) {
        StringBuilder sb = new StringBuilder();

        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            sb.append(stackTrace[i].toString() + "\n");

        }
        return sb.toString();
    }


    public void OpenLogFile() throws IOException {
        File base = new File("/opt/blastserver");
        File jobdir = new File(base, job.id);
        jobdir.mkdirs();
        File logFile = new File(jobdir, "log.txt");
        FileWriter writer = new FileWriter(logFile, true); // append
        logger = new PrintWriter(writer);
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

    public static CometClusterRunner run(String[] args) {
        Map<String, String> data = new HashMap<>();
        ClusterSession.fixLogging();
        BlastLaunchDTO dto = handleLocBlastArgs(args);
        CometClusterRunner me = new CometClusterRunner(dto, data);
        me.setParameters(args);

        me.logMessage("Starting CometClusterRunner");
        me.logMessage(" id " + dto.id);
        me.logMessage(" query " + dto.query.getAbsolutePath());
        me.logMessage(" db " + dto.database);
        me.logMessage(" out " + dto.output);

        new Thread(me).start();

        return me;
    }


    public static void main(String[] args) {
        CometClusterRunner.run(args);
    }
}




