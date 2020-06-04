package com.lordjoe.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.lordjoe.fasta.FastaTools;
import com.lordjoe.utilities.FileUtilities;

import java.io.*;
import java.util.Properties;

/**
 * com.lordjoe.ssh.SlurmRunner
 * User: Steve
 * Date: 2/5/20
 * will be the main BLAST CAller
 */
public class SlurmClusterRunner {


    public static void logMessage(String s)   {
        System.out.println(s);
    }
    public static BlastLaunchDTO handleLocBlastArgs(String[] args) {
        int index = 0;
        BLASTProgram program = BLASTProgram.BLASTP;
        String user = "xxx";
        String database = "xxx";
        String query = "xxx";
        String out = "xxx";
        if (args[index ].toLowerCase().endsWith("blastn"))
            program = BLASTProgram.BLASTN;     // todo get smarter handle more cases

        BlastLaunchDTO ret = new BlastLaunchDTO(program);
        while (index < args.length) {
            String next = args[index];
            SlurmClusterRunner.logMessage("Handling " + index + " value " + next) ;
            if (next.equalsIgnoreCase("-db")) {
                index++;
                database = args[index++];
                continue;
            }
            if (next.equalsIgnoreCase("-remote")) {
                index++;
                continue;
            }
            if (next.equalsIgnoreCase("-user")) {
                index++;
                user = args[index++];
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
        ret.output = new File(out);
        ret.query = new File(query);
        ret.format = BLASTFormat.XML2;
        ret.database = database;
        ret.user = user;
        return ret;

    }


    public static Properties getClusterProperties(String cluster) {
        try {
            Properties ret = new Properties();
            InputStream is = new FileInputStream(new File(cluster));
            ret.load(is);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public final BlastLaunchDTO job;
    public final Properties clusterProperties = getClusterProperties("ClusterLaunchCluster.properties");
    public final ClusterSession session = new ClusterSession();


    public SlurmClusterRunner(BlastLaunchDTO job) {
        this.job = job;
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
        sb.append(" " + locationOfDefaultDirectory + "/" + "SLURM_Runner.jar com.lordjoe.blast.MergeXML ");

           sb.append(locationOfDefaultDirectory + clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id);
        sb.append("/ ");
        String output = job.output.toString().replace("\\", "/");
        String outputx = output.substring(Math.max(0,output.indexOf("/") + 1));
        sb.append(locationOfDefaultDirectory + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/");
        sb.append(outputx);

        return sb.toString();
    }

    public String generateExecutionScript() {
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
        sb.append("base=${base1}.xml\n");
        sb.append("export BLASTDB=" + clusterProperties.getProperty("LocationOfDatabaseFiles") + "\n");

        String program = clusterProperties.getProperty("LocationOfBLASTPrograms") + job.program.toString().toLowerCase();
        sb.append(program);
        sb.append(" -query ");

        sb.append("${filename}");
        sb.append(" ");

        sb.append(" -db ");
        sb.append(job.database);

        sb.append("   -num_threads 32   -num_alignments 10 -evalue 1E-09 ");

        sb.append(" -outfmt ");
        sb.append(Integer.toString(job.format.code));

        sb.append(" -out ");
        sb.append(clusterProperties.getProperty("LocationOfDefaultDirectory") + clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id);
        sb.append("/");
        sb.append("${base}");

        return sb.toString();
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public void waitEmptyJobQueue(ClusterSession me)
    {
        try {
            String user = clusterProperties.getProperty("UserName");
            String answer = me.executeOneLineCommand("squeue -u " + user);
            while(answer.contains(user))  {
                answer = me.executeOneLineCommand("squeue -u " + user);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void cdDefaultDirectory(ClusterSession me) {

    }

    public void writeScripts() {
        String defaultDirectory = clusterProperties.getProperty("LocationOfDefaultDirectory");
        ClusterSession me = new ClusterSession();
        if (!me.cd(defaultDirectory))
            throw new IllegalStateException("cannot change to defaultDirectory");

        String outputDirectoryOnCluster = clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id;
        me.mkdir(outputDirectoryOnCluster);

        if (!me.cd(defaultDirectory))
            throw new IllegalStateException("cannot change to defaultDirectory");

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

        try {
            me.executeCommand("chmod a+x " + defaultDirectory + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id + "/*");
            me.executeCommand("chmod a+rw " + defaultDirectory + clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id  );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File splitQuery(File in) {
        int numberEntries = FastaTools.countFastaEntities(in);
        int splitSize = (numberEntries / 7);

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
                ClusterSession me = new ClusterSession();
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
                    String path =  directoryOnCluster +"/"  + fileName;
         //           me.prepareUpload(me.getSFTP(),path,true);
                    me.ftpFileCreate(path, is);
                }
                // cleanup local copy
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
                outDirectory.delete();

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



    private void cleanUp() {
        String directoryOnCluster = clusterProperties.getProperty("LocationOfDefaultDirectory") +
                clusterProperties.getProperty("RelativeInputDirectory") + "/" + job.id;
        ClusterSession me = new ClusterSession();
        ChannelSftp sftp = me.getSFTP();
        ClusterSession.recursiveFolderDelete(sftp,directoryOnCluster);
        directoryOnCluster = clusterProperties.getProperty("LocationOfDefaultDirectory") +
                clusterProperties.getProperty("RelativeOutputDirectory") + "/" + job.id;
        ClusterSession.recursiveFolderDelete(sftp,directoryOnCluster);
        directoryOnCluster = clusterProperties.getProperty("LocationOfDefaultDirectory") +
                clusterProperties.getProperty("RelativeScriptDirectory") + "/" + job.id;
        ClusterSession.recursiveFolderDelete(sftp,directoryOnCluster);

    }

    private void guaranteeJarFile(int call) {
        File local =  new File("SLURM_Runner.jar");
        if(!local.exists())
            throw new IllegalStateException("local jar not found");
        long size = local.length();

        try {
            String remoteFile =   clusterProperties.getProperty("LocationOfDefaultDirectory") + "SLURM_Runner.jar";
            ClusterSession me = new ClusterSession();
            ChannelSftp sftp = me.getSFTP();
            SftpATTRS fileStat = null;
            try {
                fileStat = sftp.lstat( remoteFile );
                long remotesize = fileStat.getSize() ;
                if(remotesize == size)
                    return;
            } catch (SftpException e) {
                FileInputStream is = new FileInputStream(local);
                me.ftpFileCreate(remoteFile, is);
                 if(call == 0)
                     guaranteeJarFile(call + 1);
            }
           } catch ( Exception e) {
            throw new RuntimeException(e);

        }


    }


    public static void main(String[] args) {
        try {
            ClusterSession.fixLogging();
            SlurmClusterRunner.logMessage("Starting SlurmClusterRunner");
            BlastLaunchDTO dto = handleLocBlastArgs(args);
            SlurmClusterRunner.logMessage(" id " + dto.id);
            SlurmClusterRunner.logMessage(" query " + dto.query.getAbsolutePath());
            SlurmClusterRunner.logMessage(" db " + dto.database);
            SlurmClusterRunner.logMessage(" out " + dto.output.getAbsolutePath());
            SlurmClusterRunner me = new SlurmClusterRunner(dto);

            SlurmClusterRunner.logMessage("guaranteeJarFile");
            me.guaranteeJarFile(0);


            String defaultDirectory = me.clusterProperties.getProperty("LocationOfDefaultDirectory");
            ClusterSession session = new ClusterSession();
            if (!session.cd(defaultDirectory))
                throw new IllegalStateException("cannot change to defaultDirectory");

            SlurmClusterRunner.logMessage("test default directory");
            String pwd = session.executeOneLineCommand("pwd");
            File outDirectory = me.splitQuery(me.job.query);
            me.writeScripts();
            SlurmClusterRunner.logMessage("writeScripts");

            me.transferFilesToCluster();
            SlurmClusterRunner.logMessage("transferFilesToCluster");

            SlurmClusterRunner.logMessage("Clean up directory " + outDirectory.getAbsolutePath());
            FileUtilities.expungeDirectory(outDirectory);
            SlurmClusterRunner.logMessage("clean split");

            String command = defaultDirectory + me.clusterProperties.getProperty("RelativeScriptDirectory") + "/" + me.job.id + "/" + "runBlast.sh";
            session.executeOneLineCommand(command);
            me.waitEmptyJobQueue( session);
            SlurmClusterRunner.logMessage("blast run");


            command = defaultDirectory + me.clusterProperties.getProperty("RelativeScriptDirectory") + "/" + me.job.id + "/" + "mergeXMLFiles.sh";
            session.executeOneLineCommand(command);
            me.waitEmptyJobQueue( session);
            SlurmClusterRunner.logMessage("files merged");

            ChannelSftp sftp = session.getSFTP();
            String output = me.job.output.toString().replace("\\", "/");
            String outputx = output.substring(Math.max(0,output.indexOf("/") + 1));
            StringBuilder sb = new StringBuilder();

            sb.append(me.clusterProperties.getProperty("LocationOfDefaultDirectory") + me.clusterProperties.getProperty("RelativeScriptDirectory") + "/" + me.job.id + "/");
            sb.append(outputx);

            String mergedOutput = sb.toString();
            session.ftpFileGet(me.job.output,mergedOutput);
            SlurmClusterRunner.logMessage("output fetched");

            me.cleanUp();
            System.exit(0);

            SlurmClusterRunner.logMessage(me.job.id);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            System.exit(1);

        }


    }



}

