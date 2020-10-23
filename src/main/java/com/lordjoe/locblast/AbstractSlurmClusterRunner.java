package com.lordjoe.locblast;

import com.lordjoe.ssh.ClusterSession;
import com.lordjoe.ssh.IJobRunner;
import com.lordjoe.ssh.JobState;
import com.lordjoe.ssh.SSHUserData;
import com.lordjoe.utilities.ILogger;
import com.lordjoe.utilities.SendMail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * com.lordjoe.ssh.SlurmClusterRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 */
public abstract class AbstractSlurmClusterRunner extends AbstractJobRunner {

    static Logger LOG = Logger.getLogger(AbstractSlurmClusterRunner.class.getName());

    @SuppressWarnings("unused")
     protected  boolean xml = true;




    public AbstractSlurmClusterRunner(BlastLaunchDTO job, Map<String, ? extends Object> param) {
        super(job,param);
        setXml(job.getBLASTFormat() == BLASTFormat.XML2 || job.getBLASTFormat() == BLASTFormat.XML);
        IJobRunner.registerRunner(this);
        filterProperties(param);

    }


    public static BlastLaunchDTO handleLocBlastArgs(String[] args) {
        int index = 0;
        BLASTProgram program = BLASTProgram.BLASTP;
        String database = "xxx";
        String query = "xxx";
        String out = "xxx";
        if (args[index].toLowerCase().endsWith("blastn"))
            program = BLASTProgram.BLASTN;     // todo get smarter handle more cases

        BlastLaunchDTO returnedJobData = new BlastLaunchDTO(program);
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
        returnedJobData.setOutputFileName(out);
        returnedJobData.setQuery(new File(query));
        returnedJobData.setBLASTFormat(BLASTFormat.XML2);
        returnedJobData.setJobDatabaseName(database);
        return returnedJobData;

    }


    public final Map<String, ? extends Object> filterProperties(Map<String, ? extends Object> in) {
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
        if (email != null) {
            parameters.put("email", email);
            SSHUserData user1 = SSHUserData.getUser(email);
            ClusterSession.setUser(user1);
        }
        String user = (String) in.get("user");
        if (user != null)
            parameters.put("user", email);

        return ret;
    }

    /**
     * This saves parameters making adjustments
     **/
    protected final    void adjustValue(String key, Object value, Map<String, Object> map) {
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
        BlastLaunchDTO returnedJobData = new BlastLaunchDTO(program);
        returnedJobData.setJobDatabaseName(data.get("datalib"));
        String query = "xxx";
        String out = "xxx";
        //     if (args[index].toLowerCase().endsWith("blastn"))
        program = BLASTProgram.BLASTN;     // todo get smarter handle more cases


        returnedJobData.setOutputFileName(out);
        returnedJobData.setQuery(new File(query));
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        returnedJobData.setBLASTFormat(BLASTFormat.XML2);
        return returnedJobData;

    }

 




    public boolean isXml() {
        return xml;
    }

    public void setXml(boolean xml) {
        this.xml = xml;
    }



      protected abstract  void cleanUp();

    protected final  void sendEmail(ILogger  logger) {
        String recipient = (String) parameters.get("email");
        String subjectline = "Your BLAST Analysis is complete";
        String messagebody = "The results are attached!";

        messagebody += " Output is here <a href=\"http://" + buildDownloadUrl() + "\">here</a>";


        logMessage("readyToSendEmail");
        SendMail.sendMail(recipient, subjectline, messagebody,logger);
        logMessage("emailSent");
    }

    public ILogger getLogger() {
        return new ILogger() {
            @Override
            public void log(String message) {
                logMessage(message);
            }
        };
    }


    protected String buildDownloadUrl() {
        StringBuilder sb = new StringBuilder();
        String tomcatURL = getClusterProperties().getProperty("TomcatUrl");
        sb.append(tomcatURL);
        sb.append("/SlurmProject/download");
        sb.append("?filename=");
        sb.append(job.getOutputFileName() + ".zip");
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




