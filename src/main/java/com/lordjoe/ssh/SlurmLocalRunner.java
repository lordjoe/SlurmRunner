package com.lordjoe.ssh;

import com.lordjoe.blast.BLastTools;
import com.lordjoe.blast.OSValidator;
import com.lordjoe.locblast.AbstractSlurmClusterRunner;
import com.lordjoe.locblast.BLASTProgram;
import com.lordjoe.locblast.BlastLaunchDTO;
import com.lordjoe.utilities.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * com.lordjoe.ssh.SlurmLocalRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 */
public class SlurmLocalRunner extends AbstractSlurmClusterRunner {

    public static final File BASE_DIRECTORY = new File("/opt/blastserver");
    static Logger LOG = Logger.getLogger(SlurmClusterRunner.class.getName());



    private final File jobDir;
    public SlurmLocalRunner(BlastLaunchDTO job, Map<String, ? extends Object> param) {
        super(job, param);
         jobDir = new File(BASE_DIRECTORY,job.id);
         jobDir.mkdirs();
         File listed = job.query;
         job.query = new File(jobDir,listed.getName());
         if(!job.query.getAbsoluteFile().equals(listed.getAbsoluteFile()))
             FileUtilities.copyFile(listed,job.query);
    }


    @Override
    protected void cleanUp() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }



 
    public Properties buildClusterProperties() {
        try {
            File config = new File(BASE_DIRECTORY, "config");
            File propFile = new File(config, "LocalLaunch.properties");
            Properties ret = new Properties();
            ret.load(new FileInputStream(propFile));
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    @Override
    public void run() {
        try {
            List<String> args = new ArrayList<>();
            BlastLaunchDTO job = getJob();
            BLASTProgram bp = job.program;
            Properties clusterProperties = getClusterProperties();
            String locationOfLocalBLASTPrograms = clusterProperties.getProperty("LocationOfBLASTPrograms");
            String program = locationOfLocalBLASTPrograms + job.program.toString().toLowerCase();
            String blastName = bp.toString().toLowerCase();
            if (OSValidator.isWindows()) {
                blastName = "./" + program + ".exe";
            }
            args.add(program);
            args.add("-query");
            String qpath = job.query.getAbsolutePath();
            args.add(job.query.getPath());
            args.add("-db");
            args.add(job.database.replace("-remote", ""));
            args.add("-out");
            File outFile = new File(job.query.getParentFile(),job.output);
            args.add(outFile.getAbsolutePath());
            args.add("-outfmt");
            args.add(Integer.toString(job.format.code));


            for (String parameter : parameters.keySet()) {
                if (parameter.equalsIgnoreCase("email"))
                    continue;
                if (parameter.equalsIgnoreCase("user"))
                    continue;
                if (parameter.equalsIgnoreCase("JobId"))
                    continue;
                args.add("-" + parameter);
                String value = parameters.get(parameter).toString();
                args.add(value);
            }
            setState(JobState.RunStarted);

              StringBuilder sb = new StringBuilder();
            String[] commandargs = args.toArray(new String[0]);
            for (int i = 0; i < commandargs.length; i++) {
                String commandarg = commandargs[i];
                System.out.println(commandarg + " ");
                sb.append(commandarg + " ");
            }
            System.out.println(sb.toString());
            setState(JobState.BlastCalled);
            String result = BLastTools.executeCommand(commandargs);
            logMessage(result);

            sendEmail(getLogger());
            setState(JobState.NotificationSent);

            logMessage(job.id + " is completed");
            setState(JobState.JobFinished);
           } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }

    }


    public static void main(String[] args) throws InterruptedException {
        Map<String, String> data = new HashMap<>();
        BlastLaunchDTO dto = handleLocBlastArgs(args);
        SlurmLocalRunner me = new SlurmLocalRunner(dto, data);
        me.setParameters(args);
        Thread t = new Thread(me);
        t.start();
        t.join();
        System.out.println(me.job.id);
    }
}

