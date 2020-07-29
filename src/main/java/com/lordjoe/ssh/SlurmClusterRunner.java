package com.lordjoe.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.lordjoe.fasta.FastaTools;
import com.lordjoe.utilities.FileUtilities;
import com.lordjoe.utilities.SendMail;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * com.lordjoe.ssh.SlurmClusterRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 */
public class SlurmClusterRunner implements IJobRunner {

    static Logger LOG = Logger.getLogger(SlurmClusterRunner.class.getName());

    @SuppressWarnings("unused")
    private com.jcraft.jsch.SftpException forceClassLoadSoNotFoundException;
    public final BlastLaunchDTO job;
    public Map<String, Object> parameters = new HashMap<>();
    public final static Properties clusterProperties = getClusterProperties("ClusterLaunchCluster.properties");
    private PrintWriter logger;
    private boolean xml = true;


    //   public final ClusterSession session = ClusterSession.getClusterSession();
    private final AtomicReference<JobState> state = new AtomicReference<JobState>();
    private JobState lastState;

    public static File getDefaultTomcatDirectory() {
        String localDir = SlurmClusterRunner.clusterProperties.getProperty("LocalOperatingDirectory");
        File file = new File(localDir);
        if (!file.exists())
            file.mkdirs();
        return file;
    }


    public SlurmClusterRunner(BlastLaunchDTO job, Map<String, ? extends Object> param) {
        this.job = job;
        setXml(job.format == BLASTFormat.XML2 || job.format == BLASTFormat.XML);
        IJobRunner.registerRunner(this);
        filterProperties(param);

    }


    @Override
    public void setLastState(JobState s) {
        lastState = s;
    }

    @Override
    public JobState getLastState() {
        return lastState;
    }

    public void logMessage(String s) {
        try {
            OpenLogFile();
            LOG.warning(s);
            logger.println(s);
            System.out.println(s);
            logger.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static BlastLaunchDTO handleLocBlastArgs(String[] args) {
        int index = 0;
        BLASTProgram program = BLASTProgram.BLASTP;
        String database = "xxx";
        String query = "xxx";
        String out = "xxx";
        if (args[index].toLowerCase().endsWith("blastn"))
            program = BLASTProgram.BLASTN;     // todo get smarter handle more cases

        BlastLaunchDTO ret = new BlastLaunchDTO(program);
        while (index < args.length) {
            String next = args[index];
            System.out.println("Handling " + index + " value " + next);
            if (next.equalsIgnoreCase("-user")) {
                index++;
                index++;
                continue;
            }
            if (next.equalsIgnoreCase("-email")) {
                index++;
                index++;    // skip
                continue;
            }
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
            if (next.equalsIgnoreCase("--out")) {
                index++;
                out = args[index++];
                continue;
            }
            if (next.equalsIgnoreCase("-outfmt")) {
                index++;
                index++;
                continue;
            }
            if (next.toLowerCase().contains("blast")) {
                index++;     // ignore blast program
                continue;
            }

            throw new UnsupportedOperationException("cannot handle " + next);

        }
        ret.output = out;
        ret.query = new File(query);
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        ret.format = BLASTFormat.XML2;
        ret.database = database;
        return ret;

    }


    public Map<String, ? extends Object> filterProperties(Map<String, ? extends Object> in) {
        Map<String, Object> ret = new HashMap<>();
        String[] added = BLASTProgram.relevantProperties(getJob().program);
        String prefix = BLASTProgram.prefix(getJob().program);
        for (int i = 0; i < added.length; i++) {
            String s = added[i];
            Object value = in.get(s);
            if (value != null) {
                String key = s.substring(prefix.length());
                adjustValue(key, value, ret);
            }
        }
        String email = (String) in.get("email");
        if (email != null)
            parameters.put("email", email);
        String user = (String) in.get("user");
        if (user != null)
            parameters.put("user", email);

        return ret;
    }

    /**
     * This saves parameters making adjustments
     **/
    private void adjustValue(String key, Object value, Map<String, Object> map) {
        String ret = value.toString();
        if (key.equals("gapcosts")) {
            String[] items = ret.split(",");
            map.put("gapopen", items[0]);
            parameters.put("gapopen", items[0]);
            if (items.length > 1) {
                map.put("gapextend", items[1]);
                parameters.put("gapextend", items[1]);
            }

            return;
        }
        map.put(key, value);
        parameters.put(key, value);
    }

    public void setParameters(String[] args) {
        int index = 0;
        while (index < args.length) {
            String next = args[index];
            if (next.equalsIgnoreCase("-user")) {
                index++;
                parameters.put("user", args[index++]);
                continue;
            }
            if (next.equalsIgnoreCase("-email")) {
                index++;
                parameters.put("email", args[index++]);
                continue;
            }
            index++;
        }

    }

    public static BlastLaunchDTO handleLocBlastArgs(Map<String, String> data) {
        int index = 0;
        String program_name = data.get("program");
        BLASTProgram program = BLASTProgram.fromString(program_name);
        BlastLaunchDTO ret = new BlastLaunchDTO(program);
        ret.database = data.get("datalib");
        String query = "xxx";
        String out = "xxx";
        //     if (args[index].toLowerCase().endsWith("blastn"))
        program = BLASTProgram.BLASTN;     // todo get smarter handle more cases


        ret.output = out;
        ret.query = new File(query);
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        ret.format = BLASTFormat.XML2;
        return ret;

    }


    public static Properties getClusterProperties(String cluster) {
        try {
            Properties ret = new Properties();
            InputStream resourceAsStream = SlurmClusterRunner.class.getResourceAsStream("/" + cluster);
            if (resourceAsStream != null) {
                ret.load(resourceAsStream);
                return ret;

            }
            File file = new File(cluster);
            String path = file.getAbsolutePath();
            InputStream is = new FileInputStream(file);
            ret.load(is);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public JobState getState() {
        return state.get();
    }

    public String getId() {
        return this.job.id;
    }

    public BlastLaunchDTO getJob() {
        return job;
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
        String locationOfDefaultDirectory = clusterProperties.getProperty("LocationOfDefaultDirectory");

        StringBuilder sb = new StringBuilder();
        sb.append("java -jar");

        sb.append(" " + locationOfDefaultDirectory + "SLURM_Runner.jar ");
        if(isXml())
            sb.append(" com.lordjoe.blast.MergeXML ") ;
        else
            sb.append(" com.lordjoe.blast.MergeTXT ") ;


        sb.append(locationOfDefaultDirectory + clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id);
        sb.append("/ ");
        String output = job.output.toString().replace("\\", "/");
    //    String outputx = output.substring(Math.max(0, output.indexOf("/") + 1));
        sb.append(locationOfDefaultDirectory + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/");
        sb.append(output);

        String s = sb.toString();
        return s;
    }

    public boolean isXml() {
        return xml;
    }

    public void setXml(boolean xml) {
        this.xml = xml;
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
        if(isXml())
            sb.append("base=${base1}.xml\n");
        else
            sb.append("base=${base1}.txt\n");

        sb.append("export BLASTDB=" + clusterProperties.getProperty("LocationOfDatabaseFiles") + "\n");

        String program = clusterProperties.getProperty("LocationOfBLASTPrograms") + job.program.toString().toLowerCase();
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
        String str = clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id;
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
        sb.append("export BLASTDB=" + clusterProperties.getProperty("LocationOfDatabaseFiles") + "\n");
        sb.append("for file in ");
        sb.append(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id);
        sb.append("/*\n");

        sb.append("do\n");
        sb.append("filename=${file}\n");
        String jobScript = clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/submitToCPUNode.sh";
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

    private String makeSampleSubmitToCPU(String data) {
        String inputLocationDir = clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id;
        String inputLocation = inputLocationDir + "/splitFile001.faa";
        data = data.replace("${filename}", inputLocation);
        if(isXml())
            data = data.replace("${base}", "splitFile001.xml");
        else
            data = data.replace("${base}", "splitFile001.txt");

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

    public static Set<Integer> getJobNumbers(ClusterSession me) {
        String user = clusterProperties.getProperty("UserName");
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

    private static Integer parseJobId(String item) {
        item = item.trim();
        int index = item.indexOf(" ");
        String s = item.substring(0, index).trim();
        return new Integer(s);
    }

    public void waitEmptyJobQueue(ClusterSession me, Set<Integer> priors) {
        justSleep(2000); // make sure we have jobs
        String user = clusterProperties.getProperty("UserName");
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
            String defaultDirectory = clusterProperties.getProperty("LocationOfDefaultDirectory");
            ClusterSession me = ClusterSession.getClusterSession();
            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

            String outputDirectoryOnCluster = clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id;
            me.mkdir(outputDirectoryOnCluster);


            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

            String directoryOnCluster = clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id;
            me.mkdir(directoryOnCluster);


            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");
            directoryOnCluster = clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id;
            me.mkdir(directoryOnCluster);


            writeExecutionScript(me);
            writeMergerScript(me);
            writeIterationScript(me);


            if (!me.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

            me.executeOneLineCommand("chmod a+x " + defaultDirectory + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/*");
            me.executeOneLineCommand("chmod a+rw " + defaultDirectory + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id);
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

        File outDirectory = new File(clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id);
        outDirectory.mkdirs();

        String baseName = "splitFile";
        FastaTools.splitFastaFile(in, outDirectory, baseName, splitSize, numberEntries);
        return outDirectory;
    }


    public void transferFilesToCluster() {
        try {
            File outDirectory = new File(clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id);
            File[] files = outDirectory.listFiles();
            if (files != null) {
                String directoryOnCluster = clusterProperties.getProperty("LocationOfDefaultDirectory") +
                        clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id;
                ClusterSession me = ClusterSession.getClusterSession();
                me.mkdir(directoryOnCluster);

//                try {
//                    ChannelSftp sftp = me.getSFTP();
//                    sftp.cd(clusterProperties.getProperty("RelativeInputDirectory"));
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


    private void cleanUp() {
        String directoryOnCluster = clusterProperties.getProperty("LocationOfDefaultDirectory") +
                clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id;
        ClusterSession me = ClusterSession.getClusterSession();
        ChannelSftp sftp = me.getSFTP();
        ClusterSession.recursiveFolderDelete(sftp, directoryOnCluster);
        directoryOnCluster = clusterProperties.getProperty("LocationOfDefaultDirectory") +
                clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id;
        ClusterSession.recursiveFolderDelete(sftp, directoryOnCluster);
        directoryOnCluster = clusterProperties.getProperty("LocationOfDefaultDirectory") +
                clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id;
        ClusterSession.recursiveFolderDelete(sftp, directoryOnCluster);

        ClusterSession.releaseClusterSession(me);
    }

    private void guaranteeJarFile(int call) {
        File defaultDir = new File("/opt/blastserver");
        File local = new File(defaultDir, "SLURM_Runner.jar");
        if (!local.exists()) {
            String path = local.getAbsolutePath();
            throw new IllegalStateException("local jar not found at " + path);
        }
        long size = local.length();
        logMessage("LocalFileFound");
        try {
            String remoteFile = clusterProperties.getProperty("LocationOfDefaultDirectory") + "SLURM_Runner.jar";
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

    private void sendEmail() {
        String recipient = (String) parameters.get("email");
        String subjectline = "Your BLAST Analysis is complete";
        String messagebody = "The results are attached!";

        messagebody += "output is here " + buildDownloadUrl();

        logMessage("readyToSendEmail");
        SendMail.sendMail(recipient, subjectline, messagebody);
        logMessage("emailSent");
    }

    private String buildDownloadUrl() {
        StringBuilder sb = new StringBuilder();
        String tomcatURL = clusterProperties.getProperty("TomcatUrl");
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
            String defaultDirectory = clusterProperties.getProperty("LocationOfDefaultDirectory");


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

            String command = defaultDirectory + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/" + "runBlast.sh";
            setState(JobState.BlastCalled);

            session.executeOneLineCommand(command);
            waitEmptyJobQueue(session, priors);
            logMessage("blast run");
            setState(JobState.BlastFinished);

            // what is the user running before we start
            priors = getJobNumbers(session);

            command = "salloc " + defaultDirectory + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/" + "mergeXMLFiles.sh";
            session.executeOneLineCommand(command);

            waitEmptyJobQueue(session, priors);
            logMessage("files merged");
            setState(JobState.FilesMerged);


            //     ChannelSftp sftp = session.getSFTP();
            String output = job.output.toString().replace("\\", "/");
            String outputx = output.substring(Math.max(0, output.indexOf("/")));
            StringBuilder sb = new StringBuilder();

            sb.append(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/");
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

    private void setState(JobState j) {
        logMessage(j.toString());
        state.set(j);
    }

    public void OpenLogFile() throws IOException {
        File base = new File("/opt/blastserver");
        File jobdir = new File(base, job.id);
        jobdir.mkdirs();
        File logFile = new File(jobdir, "log.txt");
        FileWriter writer = new FileWriter(logFile, true); // append
        logger = new PrintWriter(writer);
    }


    public static SlurmClusterRunner run(Map<String, String> data) {
        ClusterSession.fixLogging();
        BlastLaunchDTO dto = handleLocBlastArgs(data);
        SlurmClusterRunner me = new SlurmClusterRunner(dto, data);
        me.logMessage("Starting SlurmClusterRunner");
        me.logMessage(" id " + dto.id);
        me.logMessage(" query " + dto.query.getAbsolutePath());
        me.logMessage(" db " + dto.database);
        me.logMessage(" out " + new File(dto.getLocalJobDirectory(), dto.output));

        new Thread(me).start();

        return me;
    }

    public static SlurmClusterRunner run(String[] args) {
        Map<String, String> data = new HashMap<>();
        ClusterSession.fixLogging();
        BlastLaunchDTO dto = handleLocBlastArgs(args);
        SlurmClusterRunner me = new SlurmClusterRunner(dto, data);
        me.setParameters(args);

        me.logMessage("Starting SlurmClusterRunner");
        me.logMessage(" id " + dto.id);
        me.logMessage(" query " + dto.query.getAbsolutePath());
        me.logMessage(" db " + dto.database);
        me.logMessage(" out " + dto.output);

        new Thread(me).start();

        return me;
    }


    public static void main(String[] args) {
        SlurmClusterRunner.run(args);
    }
}




