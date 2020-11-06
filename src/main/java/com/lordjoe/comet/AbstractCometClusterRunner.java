 
package com.lordjoe.comet;

import com.jcraft.jsch.SftpException;
import com.lordjoe.fasta.FastaTools;
import com.lordjoe.locblast.AbstractJobRunner;
import com.lordjoe.ssh.IJobRunner;
import com.lordjoe.ssh.JobState;
import com.lordjoe.ssh.SSHUserData;
import com.lordjoe.utilities.FileUtilities;
import com.lordjoe.utilities.ILogger;
import com.lordjoe.utilities.SendMail;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
/**
 * com.lordjoe.ssh.SlurmClusterRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 */ 

public abstract class AbstractCometClusterRunner extends AbstractJobRunner {


    @SuppressWarnings("unused")
    protected  SftpException forceClassLoadSoNotFoundException;
     protected Map<String, Object> parameters = new HashMap<>();
      public final CometLaunchDTO job;


    //   public final ClusterSession session = ClusterSession.getClusterSession();
    protected  final AtomicReference<JobState> state = new AtomicReference<JobState>();
    protected    JobState lastState;




    public AbstractCometClusterRunner(CometLaunchDTO job, Map<String, ? extends Object> param) {
        super(job.id,param);
        this.job = job;
         IJobRunner.registerRunner(this);
        filterProperties(param);

    }

 
    @Override
    public final CometLaunchDTO getJob() {
        return job;
    }

   
/**
     * read a fasta file return the number of fields
     *
     * @param rdr
     * @return
     */ 

    public static int countMGFEntities(File f) {
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(f));
            return countMGFEntities(rdr);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

 
/**
     * read a fasta file return the number of fields
     *
     * @param rdr
     * @return
     */ 

    public static int countMGFEntities(List<File> input) {
        int ret = 0;
        for (File file : input) {
            ret += countMGFEntities(file);
        }
        return ret;
    }


     
/**
     * read a fasta file return the number of fields
     *
     * @param rdr
     * @return
     */ 

    public static int countMGFEntities(LineNumberReader rdr) {
        try {
            int ret = 0;
            String line = rdr.readLine();
            while (line != null) {
                if (line.startsWith("BEGIN IONS"))
                    ret++;
                line = rdr.readLine();
            }
            rdr.close();
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public File splitSpectra(File in,File outDirectory) {
        int numberEntries = countMGFEntities(in);

        int splitSize = numberEntries;
        if (numberEntries > 70)
            splitSize = (numberEntries / 7);

        outDirectory.mkdirs();
        outDirectory.setReadable(true,true);

        String baseName = "splitFile";
        splitMGFFile(in, outDirectory, baseName, splitSize, numberEntries);
        return outDirectory;
    }


    public static void splitMGFFile(File in, File outDirectory, String baseName, int splitSize, int numberEntries) {
        splitMGFFile(in, outDirectory, baseName, splitSize,numberEntries, Integer.MAX_VALUE);
    }

    public static void splitMGFFile(File in, File outDirectory, String baseName, int splitSize,  int numberEntries,int maxsplits) {
        try {
            LineNumberReader rdr = FastaTools.getReader(in);
            if (!outDirectory.exists()) {
                if (!outDirectory.mkdirs())
                    throw new UnsupportedOperationException("Cannot make output directory " + outDirectory);
            }
            int numberSplits =   numberEntries/ splitSize;
            rdr = FastaTools.getReader(in);
            String line = rdr.readLine();
            for (int i = 0; i < numberSplits; i++) {
                String index = String.format("%03d", i + 1);
                File outFile = new File(outDirectory, baseName + index + ".mgf");
                System.out.println(outFile.getAbsolutePath());
                PrintWriter out = FastaTools.getWriter(outFile);
                if(i + 1 == numberSplits)
                    splitSize = Integer.MAX_VALUE;
                line = writeSplit(out, rdr, line, splitSize);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
    protected static String writeSplit(PrintWriter out, LineNumberReader rdr, String line, int splitSize) throws IOException {
        int count = 0;
        while (line != null) {
            if (line.startsWith("BEGIN IONS")) {
                count++;
                if (count > splitSize) {
                    out.close();
                    return line;
                }
            }
            out.println(line);
            line = rdr.readLine();
        }
        out.close();
        return line;
    }

    public static CometLaunchDTO handleLocBlastArgs(String[] args) {
        int index = 0;
          String database = "xxx";
        String query = "xxx";
        String params = "xxx";

        CometLaunchDTO ret = new CometLaunchDTO( );
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
                params = args[index++];
                continue;
            }
              if (next.equalsIgnoreCase("-query")) {
                index++;
                query = args[index++];
                continue;
            }
             if (next.toLowerCase().contains("comet")) {
                index++;     // ignore blast program
                continue;
            }

            throw new UnsupportedOperationException("cannot handle " + next);

        }
       ret.setSpectra( new File(query));
        ret.setJobDatabaseName(database);
        ret.setParams(new File(params));
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


    public static byte[] getZorzanKey() {
        File pk = new File("/opt/blastserver/HPC.ppk");
        return FileUtilities.readFileBytes(pk);
    }
  ;



    public final Map<String, ? extends Object> filterProperties(Map<String, ? extends Object> in) {
        Map<String, Object> ret = new HashMap<>();


        String email = (String) in.get("email");
        if (email != null) {
            parameters.put("email", email);
            SSHUserData user1 = SSHUserData.getUser(email);
               parameters.put("user", user1.userName);
           }
        else {
            String user = (String) in.get("user");
            if (user != null)
                parameters.put("user", email);

        }

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

    public final static CometLaunchDTO handleLocBlastArgs(Map<String, String> data) {
        int index = 0;
        String program_name = data.get("program");
        CometLaunchDTO ret = new CometLaunchDTO( );
        ret.setJobDatabaseName(data.get("datalib"));
        String query = "xxx";
        String out = "xxx";
        //     if (args[index].toLowerCase().endsWith("blastn"))


        ret.setJobDatabaseName(out);
        ret.setSpectra(new File(query));
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        return ret;

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

        messagebody += " Output is <a href=\"http://" + buildDownloadUrl() + "\">here</a>";

        logMessage("readyToSendEmail");
        SendMail.sendMail(recipient, subjectline, messagebody,log);
        logMessage("emailSent");
    }


    protected final String buildDownloadUrl() {
        StringBuilder sb = new StringBuilder();
        String tomcatURL = getClusterProperties().getProperty("TomcatUrl");
        sb.append(tomcatURL);
        sb.append("/SlurmProject/download");
        sb.append("?filename=");
        sb.append(getOutputName());
        sb.append("&directory=");
        sb.append(job.id);

        String s = sb.toString();
        return s;
    }


    public String getOutputName() {
        String name = job.getSpectra().getName();
        return name.replace(".mgf",".pep.xml");
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

  }




 
