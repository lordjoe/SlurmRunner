package com.lordjoe.fasta;

import com.devdaily.system.SystemCommandExecutor;
import com.lordjoe.blast.OSValidator;
import com.lordjoe.ssh.BLASTFormat;
import com.lordjoe.ssh.BLASTProgram;
import com.lordjoe.ssh.BlastJob;
import com.lordjoe.ssh.BlastLaunchDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * com.lordjoe.fasta.LocalJobRunner
 * Take a job specification and run locally
 * User: Steve
 * Date: 1/27/2020
 */
public class LocalJobRunner {

    private static Properties clusterProperties;

    public static Properties getClusterProperties(String cluster) {
        try {
            Properties ret = new Properties();
            if (cluster == null) {
                InputStream is = LocalJobRunner.class.getResourceAsStream("/com/lordjoe/blast/ClusterLaunchAsterix.properties");
                ret.load(is);
            } else {
                InputStream is = LocalJobRunner.class.getResourceAsStream("/com/lordjoe/blast/ClusterLaunchCluster.properties");
                ret.load(is);
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public List<String>  buildCommandList(BlastLaunchDTO dto)
    {
        Properties cluster = getClusterProperties(null);
        String program = cluster.getProperty("LocationOfBLASTPrograms") + dto.program.toString().toLowerCase();

        List<String> ret = new ArrayList<>();

        if(OSValidator.isWindows())
            ret.add(program + ".exe");
        else
            ret.add(program);
        ret.add("-query");
        String query = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeInputDirectory") + "/" + dto.query.getName();
        ret.add(query);



        ret.add("-db");
        String db = cluster.getProperty("LocationOfDatabaseFiles") + dto.database;
        ret.add(db);

        ret.add("-outfmt");
        String fmt = "" + dto.format.code;
        ret.add(fmt);


        ret.add("-out");
        String out = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeOutputDirectory") + "/" + dto.output.getName();
        ret.add(out) ;

        return ret;
    }

    public String buildCommandString(BlastLaunchDTO dto)
    {
        Properties cluster = getClusterProperties(null);
        String program = cluster.getProperty("LocationOfBLASTPrograms") + dto.program.toString().toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append(program);
        if(OSValidator.isWindows())
            sb.append(".exe");
        sb.append(" -query ");
        String query = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeInputDirectory") + "/" + dto.query.getName();
          sb.append(query);

        sb.append(" -db ");
        String db = cluster.getProperty("LocationOfDatabaseFiles") + dto.database;
        sb.append(db);

        sb.append(" -outfmt ");
        String fmt = "" + dto.format.code;
        sb.append(fmt);


        sb.append(" -out ");
        String out = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeOutputDirectory") + "/" + dto.output.getName();
        sb.append(out) ;

        return sb.toString();
    }

    public BlastJob runLocalBlastPJob(File query, String database, File output) {
        try {
            BlastLaunchDTO dto = new BlastLaunchDTO(BLASTProgram.BLASTP);
            dto.query = query;
            dto.database = database;
            dto.format = BLASTFormat.XML2;
            dto.output = output;
            List<File> tempFiles = new ArrayList<>();
            tempFiles.add(output);

            BlastJob ret = new BlastJob(dto, tempFiles, output);
            Properties cluster = getClusterProperties(null);
            String program = cluster.getProperty("LocationOfBLASTPrograms") + dto.program.toString().toLowerCase();
            String command  = buildCommandString(dto);
            System.out.println(command);
            List<String> commandInformation = buildCommandList(dto);
            SystemCommandExecutor executor  =  new SystemCommandExecutor(commandInformation);
            executor.executeCommand();
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
    }   public BlastJob runLocalBlastNJob(File query, String database, File output) {
        try {
            BlastLaunchDTO dto = new BlastLaunchDTO(BLASTProgram.BLASTN);
            dto.query = query;
            dto.database = database;
            dto.format = BLASTFormat.XML2;
            dto.output = output;
            List<File> tempFiles = new ArrayList<>();
            tempFiles.add(output);

            BlastJob ret = new BlastJob(dto, tempFiles, output);
            Properties cluster = getClusterProperties(null);
            String program = cluster.getProperty("LocationOfBLASTPrograms") + dto.program.toString().toLowerCase();
            String command  = buildCommandString(dto);
            System.out.println(command);
            List<String> commandInformation = buildCommandList(dto);
            SystemCommandExecutor executor  =  new SystemCommandExecutor(commandInformation);
            executor.executeCommand();
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
    }

    public static void main(String[] args) {
        int index = 0;
        File query = new File(args[index++]);
        String database = args[index++];
        File output = new File(args[index++]);

        LocalJobRunner me = new LocalJobRunner();

         BlastJob job =  me.runLocalBlastNJob(  query,   database,   output);
         if(job.finished())
             return;
    }

}
