package com.lordjoe.locblast;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.lordjoe.ssh.ClusterSession;
import com.lordjoe.ssh.IJobRunner;
import com.lordjoe.ssh.JobState;

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
public abstract class AbstractJobRunner  implements IJobRunner {

    private static Logger LOG = Logger.getLogger(AbstractJobRunner.class.getName());


    protected SftpException forceClassLoadSoNotFoundException;
    public final BlastLaunchDTO job;
    public Map<String, Object> parameters = new HashMap<>();
    public final   Properties clusterPropertiesX = buildClusterProperties( );
    protected PrintWriter logger;
    //   public final ClusterSession session = ClusterSession.getClusterSession();
    protected  final AtomicReference<JobState> state = new AtomicReference<JobState>();
    protected    JobState lastState;

     public   Properties buildClusterProperties() {
        try {
            Properties ret = new Properties();
            ret.load(new FileInputStream("/opt/blastserver/ClusterLaunch.properties"));
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public AbstractJobRunner(BlastLaunchDTO job, Map<String, ? extends Object> param) {
        this.job = job;
          IJobRunner.registerRunner(this);

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


    public void OpenLogFile() throws IOException {
        File base = new File("/opt/blastserver");
        File jobdir = new File(base, job.id);
        jobdir.mkdirs();
        jobdir.setReadable(true,true);
        File logFile = new File(jobdir, "log.txt");
        FileWriter writer = new FileWriter(logFile, true); // append
        logger = new PrintWriter(writer);
    }



    public final void logMessage(String s) {
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
                me.ftpFileCreate(remoteFile, is,0666);
                logMessage("Remote Jar Downloaded");
                if (call == 0)
                    guaranteeJarFile(call + 1);
            }
            ClusterSession.releaseClusterSession(me);

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

    @Override
    public final BlastLaunchDTO getJob() {
        return job;
    }


    public final JobState getState() {
        return state.get();
    }

    public final String getId() {
        return this.job.id;
    }



    @Override
    public abstract Map<String, ? extends Object> filterProperties(Map<String, ?> in);

    @Override
    public abstract void run();
}
