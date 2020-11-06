package com.lordjoe.locblast;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.lordjoe.ssh.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * com.lordjoe.ssh.AbstractJobRunner
 * User: Steve
 * Date: 10/19/20
 */
public abstract class AbstractJobRunner implements IJobRunner {

    private static Logger LOG = Logger.getLogger(AbstractJobRunner.class.getName());


    public final String id;
    protected SftpException forceClassLoadSoNotFoundException;
    public Map<String, Object> parameters = new HashMap<>();
    public final Properties clusterPropertiesX = buildClusterProperties();
    protected PrintWriter logger;
    //   public final ClusterSession session = ClusterSession.getClusterSession();
    protected final AtomicReference<JobState> state = new AtomicReference<JobState>();
    protected JobState lastState;
    private  ClusterSession gSession;
    private  boolean inUse;
    private final SSHUserData user;


    public AbstractJobRunner(String id, Map<String, ?  > paramX) {
        Map<String,String> param = ( Map<String,String>)paramX;
        this.id = id;
        IJobRunner.registerRunner(this);
        OpenLogFile();
        String email = (String) param.get("email");
        if (email == null)
            throw new UnsupportedOperationException("Email is required");
        param.put("email", email);
        user = SSHUserData.getUser(email);
        if (user == null)
            throw new UnsupportedOperationException("user is required");
        param.put("user",  user.userName);

    }

    public synchronized  ClusterSession getClusterSession() {
        if (gSession == null) {
            gSession = new ClusterSession(getUser());
        }
        if (inUse)
            throw new UnsupportedOperationException("Session In Use");
        inUse = true;
        return gSession;

    }

    public     void releaseClusterSession(ClusterSession me) {
        inUse = false;
    }

    public   SSHUserData getUser() {
        return user;
    }

     public static Properties readClusterProperties() {
        try {
            Properties ret = new Properties();
            ret.load(new FileInputStream("/opt/blastserver/ClusterLaunch.properties"));
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public  Properties buildClusterProperties() {
        return readClusterProperties();
    }


    public final Properties getClusterProperties() {
        return clusterPropertiesX;
    }

    public final File getDefaultTomcatDirectory() {
        String localDir = getClusterProperties().getProperty("LocalOperatingDirectory");
        File file = new File(localDir);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public final String getId() {
        return id;
    }


    protected static final Integer parseJobId(String item) {
        try {
            item = item.trim();
            int index = item.indexOf(" ");
            String s = item.substring(0, index).trim();
            return new Integer(s);
        } catch (NumberFormatException e) {
            return 0;

        }
    }


    public void OpenLogFile() {
        try {
            File base = new File("/opt/blastserver");
            File jobdir = new File(base, getId());
            jobdir.mkdirs();
            jobdir.setReadable(true, true);
            File logFile = new File(jobdir, "log.txt");
            FileWriter writer = new FileWriter(logFile, true); // append
            logger = new PrintWriter(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * comput the file to upload which may be zipped from the job
     *
     * @param job
     * @return
     */
    public abstract String getClusterMergeResultZipFileName(LaunchDTO job);


    public final void logMessage(String s) {
             OpenLogFile();
            LOG.warning(s);
            logger.println(s);
            System.out.println(s);
            logger.close();
     }

    protected final void guaranteeJarFile(int call) {
        File defaultDir = new File("/opt/blastserver");
        File local = new File(defaultDir, "SLURM_Runner.jar");
        if (!local.exists()) {
            String path = local.getAbsolutePath();
            throw new IllegalStateException("local jar not found at " + path);
        }
        long size = local.length();
        logMessage("LocalFileFound");
        try {
            String remoteFile = getClusterProperties().getProperty("LocationOfDefaultDirectory") + "SLURM_Runner.jar";
            ClusterSession me = getClusterSession();
            ChannelSftp sftp = me.getSFTP();
            SftpATTRS fileStat = null;
            try {
                fileStat = sftp.lstat(remoteFile);
                long remotesize = fileStat.getSize();
                if (remotesize == size) {
                     releaseClusterSession(me);
                    logMessage("Remote Jar Same");
                    return;
                }
            } catch (SftpException e) {
                FileInputStream is = new FileInputStream(local);
                me.ftpFileCreate(remoteFile, is, 0666);
                logMessage("Remote Jar Downloaded");
                if (call == 0)
                    guaranteeJarFile(call + 1);
            }
             releaseClusterSession(me);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }


    }

    @Override
    public final void setLastState(JobState s) {
        lastState = s;
    }

    @Override
    public final JobState getLastState() {
        return lastState;
    }


    public final JobState getState() {
        return state.get();
    }


    @Override
    public abstract Map<String, ? extends Object> filterProperties(Map<String, ?> in);

    @Override
    public abstract void run();
}
