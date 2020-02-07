package com.lordjoe.fasta;

import com.devdaily.system.SystemCommandExecutor;
import com.lordjoe.blast.BLastTools;
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
                InputStream is;
                if(OSValidator.isWindows())
                  is = LocalJobRunner.class.getResourceAsStream("/com/lordjoe/blast/ClusterLaunchAsterix.properties");
                else
                    is = LocalJobRunner.class.getResourceAsStream("/com/lordjoe/blast/ClusterLaunchAsterixLinux.properties");
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


    public static File getInputDirectory() {
        Properties cluster = getClusterProperties(null);

        File ret =  new File(cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeInputDirectory"));
        if(ret.exists() && ret.isDirectory())
            return ret;
        ret.mkdirs();
        if(ret.exists() && ret.isDirectory())
            return ret;
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public static File getOutputDirectory() {
        Properties cluster = getClusterProperties(null);

        File ret =  new File(cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeOutputDirectory"));
        if(ret.exists() && ret.isDirectory())
            return ret;
        ret.mkdirs();
        if(ret.exists() && ret.isDirectory())
            return ret;
        throw new UnsupportedOperationException("Fix This"); // ToDo
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
        String query;
        if(dto.query.getParentFile() == null)
           query = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeInputDirectory") + "/" + dto.query.getName();
        else
            query   = dto.query.getAbsolutePath();
       ret.add(query);



        ret.add("-db");
        String db = /* cluster.getProperty("LocationOfDatabaseFiles")  + */ dto.database;
        ret.add(db);

        ret.add("-outfmt");
        String fmt = "" + dto.format.code;
        ret.add(fmt);


        ret.add("-out");
        String out;
        if(dto.output.getParentFile() == null)
            out = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeInputDirectory") + "/" + dto.output.getName();
        else
            out   = dto.output.getAbsolutePath();

         ret.add(out) ;

        return ret;
    }

    public File buildInputDirectory(BlastLaunchDTO dto)
    {
        Properties cluster = getClusterProperties(null);
        String query = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeInputDirectory") + "/" + dto.id;
        return new File(query);
    }

    public File buildOutputDirectory(BlastLaunchDTO dto)
    {
        Properties cluster = getClusterProperties(null);
        String query = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeOutputDirectory") + "/" + dto.id;
        return new File(query);
    }

    public String buildCommandString(BlastLaunchDTO dto,boolean isSplit)
    {
        Properties cluster = getClusterProperties(null);
        String program = cluster.getProperty("LocationOfBLASTPrograms") + dto.program.toString().toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append(program);
        if(OSValidator.isWindows())
            sb.append(".exe");
        sb.append(" -query ");
        String insert = "/";
        if(isSplit )
            insert += dto.id + "/";
        String query = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeInputDirectory") + insert + dto.query.getName();
          sb.append(query);

        sb.append(" -db ");
        String db = cluster.getProperty("LocationOfDatabaseFiles") + dto.database;
        sb.append(db);

        sb.append(" -outfmt ");
        String fmt = "" + dto.format.code;
        sb.append(fmt);


        sb.append(" -out ");
        String out = cluster.getProperty("LocationOfDefaultDirectory") + cluster.getProperty("RelativeOutputDirectory") + insert + dto.output.getName();
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
            boolean splitFiles = false;
            String command  = buildCommandString(dto,splitFiles);
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
    }    public BlastJob runSplitBlastPJob(File query, String database, File output) {
        try {
            BlastLaunchDTO dto = new BlastLaunchDTO(BLASTProgram.BLASTP);
            dto.query = query;
            dto.database = database;
            dto.format = BLASTFormat.XML2;
            dto.output = output;


            File parentFile = query.getParentFile();
            if(parentFile == null)
                parentFile =  getInputDirectory();

            File splitDirectory = new File(parentFile,dto.id);
            if(!splitDirectory.mkdirs())
                throw new UnsupportedOperationException("cannot make directory " + dto.id);

            File parentFile1 = output.getParentFile();
            if(parentFile1 == null)
                parentFile1 = getOutputDirectory();
            File outSplitDirectory = new File(parentFile1,dto.id);
            if(!outSplitDirectory.mkdirs())
                throw new UnsupportedOperationException("cannot make directory " + outSplitDirectory.getAbsolutePath());

            int numberEntries = FastaTools.countFastaEntities(query);
            int splitsize =  (numberEntries / 7);

            FastaTools.splitFastaFile(query,splitDirectory,"split",splitsize,numberEntries);

            File[] files = splitDirectory.listFiles();
            List<File> tempFiles = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                tempFiles.add(file);
            }

            BlastJob ret = new BlastJob(dto, tempFiles, output);

            for (File tempFile : tempFiles) {
                String outName = BlastLaunchDTO.makeOutputName(tempFile);
                File outFile = new File(outSplitDirectory,outName) ;
                BlastLaunchDTO newdto =  dto.withNewQuery(tempFile,outFile);
                List<String> commandInformation = buildCommandList(newdto);
                for (String s : commandInformation) {
                    System.out.print (s + " ");
                }
                System.out.println();
                SystemCommandExecutor executor  =  new SystemCommandExecutor(commandInformation);
                int i = executor.executeCommand();
                if(i != 0)  {
                    throw new IllegalStateException("command failed " + executor.getStandardErrorFromCommand());
                }

            }


            BLastTools.mergeXMLFiles_P(output.getAbsolutePath(),outSplitDirectory.listFiles());

            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
    }




    public BlastJob runLocalBlastNJob(File query, String database, File output) {
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
            String command  = buildCommandString(dto,false);
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

       //  BlastJob job =  me.runLocalBlastPJob(  query,   database,   output);
         BlastJob job = me.runSplitBlastPJob(  query,   database,   output);
         if(job.finished())
             return;
    }

}
