package com.lordjoe.comet;

import com.jcraft.jsch.SftpException;
import com.lordjoe.ssh.*;
import com.lordjoe.utilities.ILogger;
import com.lordjoe.utilities.SendMail;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * com.lordjoe.ssh.SlurmClusterRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 */
public abstract class AbstractCometClusterRunner implements IJobRunner {

    static Logger LOG = Logger.getLogger(AbstractCometClusterRunner.class.getName());

    @SuppressWarnings("unused")
    protected  SftpException forceClassLoadSoNotFoundException;
    public final BlastLaunchDTO job;
    public Map<String, Object> parameters = new HashMap<>();
    public final   Properties clusterPropertiesX = buildClusterProperties( );
    protected  PrintWriter logger;


    //   public final ClusterSession session = ClusterSession.getClusterSession();
    protected  final AtomicReference<JobState> state = new AtomicReference<JobState>();
    protected    JobState lastState;

    public Properties getClusterProperties() {
        return clusterPropertiesX;
    }

    public File getDefaultTomcatDirectory() {
        String localDir = getClusterProperties().getProperty("LocalOperatingDirectory");
        File file = new File(localDir);
        if (!file.exists())
            file.mkdirs();
        return file;
    }


    public AbstractCometClusterRunner(BlastLaunchDTO job, Map<String, ? extends Object> param) {
        this.job = job;
         IJobRunner.registerRunner(this);
        filterProperties(param);

    }


    @Override
    public void setLastState(JobState s) {
        lastState = s;
    }

    @Override
    public final JobState getLastState() {
        return lastState;
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

    public static BlastLaunchDTO handleLocBlastArgs(String[] args) {
        int index = 0;
        BLASTProgram program = BLASTProgram.COMET;
        String database = "xxx";
        String query = "xxx";
        String out = "xxx";

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
            if (next.equalsIgnoreCase("-params")) {
                index++;
                continue;
            }
              if (next.equalsIgnoreCase("-query")) {
                index++;
                query = args[index++];
                continue;
            }
            if (next.equalsIgnoreCase("-outFolder")) {
                index++;
                out = args[index++];
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
            if (next.toLowerCase().contains("comet")) {
                index++;     // ignore blast program
                continue;
            }

            throw new UnsupportedOperationException("cannot handle " + next);

        }
        ret.output = out;
        ret.query = new File(query);
        ret.format = BLASTFormat.XML2;
        ret.database = database;
        return ret;

    }

    public final static  Map<String, ? extends Object> buildParameters(String[] args) {
        Map<String, Object> ret = new HashMap<>();
        int index = 0;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i++];
            String value = args[i];
            ret.put(arg.substring(1),value);
        }

        return ret;
    }


    public final Map<String, ? extends Object> filterProperties(Map<String, ? extends Object> in) {
        Map<String, Object> ret = new HashMap<>();
        String[] added = BLASTProgram.relevantProperties(getJob().program);
        String prefix = BLASTProgram.prefix(getJob().program);

        String email = (String) in.get("email");
        if (email != null)
            parameters.put("email", email);
        String user = (String) in.get("user");
        if (user != null)
            parameters.put("user", email);

        return ret;
    }



    public final void setParameters(String[] args) {
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

    public final static BlastLaunchDTO handleLocBlastArgs(Map<String, String> data) {
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



    public final Properties buildClusterProperties() {
        try {
            Properties ret = new Properties();
            ret.load(new FileInputStream("/opt/blastserver/ClusterLaunch.properties"));
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






    public ILogger getLogger() {
        return new ILogger() {
            @Override
            public void log(String message) {
                logMessage(message);
            }
        };
    }



    protected abstract  void cleanUp();

    protected final  void sendEmail(ILogger log) {
        String recipient = (String) parameters.get("email");
        String subjectline = "Your BLAST Analysis is complete";
        String messagebody = "The results are attached!";

        messagebody += " Output is here <a href=\"http://" + buildDownloadUrl() + "\">here</a>";

        logMessage("readyToSendEmail");
        SendMail.sendMail(recipient, subjectline, messagebody,log);
        logMessage("emailSent");
    }


    protected final String buildDownloadUrl() {
        StringBuilder sb = new StringBuilder();
        String tomcatURL = getClusterProperties().getProperty("TomcatUrl");
        sb.append(tomcatURL);
        sb.append("?filename=");
        sb.append(job.output);
        sb.append("&directory=");
        sb.append(job.id);

        return sb.toString();
    }





    public static String getStaceTraceString(Exception e) {
        StringBuilder sb = new StringBuilder();

        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            sb.append(stackTrace[i].toString() + "\n");

        }
        return sb.toString();
    }

    protected final  void setState(JobState j) {
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

  }




