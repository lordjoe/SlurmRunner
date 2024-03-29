package com.lordjoe.locblast;

import com.lordjoe.blast.OSValidator;
import com.lordjoe.ssh.LaunchDTO;

import java.io.File;
import java.util.UUID;

/**
 * com.lordjoe.ssh.BlastLaunchDTO
 * User: Steve
 * Date: 1/25/20
 */
public class BlastLaunchDTO extends LaunchDTO  {
    public final BLASTProgram program;
    private String database;
    private File query;   // original file
    private BLASTFormat format;
    public String email;
    public String user;
    private String output; //the output filename. Output file will always be a zip file
    private int commandProcessors = 0;

    public String getJobDatabaseName() {
        return database;
    }

    public long getDatabaseSize() {
        if("swissprot".equalsIgnoreCase(database))
            return  176700347L;
        if("nr".equalsIgnoreCase(database))
            return  61228200318L;
        if(database.startsWith("shortened"))
            return  10000000L;
        return queryDBSize(database);
    }

    public int getCommandProcessors() {
       return commandProcessors;
    }

    public void setCommandProcessors(int commandProcessors) {
        this.commandProcessors = commandProcessors;
    }

    private static long queryDBSize(String database) {
        throw new UnsupportedOperationException("write code to call blastdbcmd -info -db blastDBs/<database>"); // ToDo
    }

    public void setJobDatabaseName(String database) {
        this.database = database;
    }

    public File getQuery() {
        return query;
    }

    public void setQuery(File query) {
        this.query = query;
    }

    public BLASTFormat getBLASTFormat() {
        return format;
    }

    public void setBLASTFormat(BLASTFormat format) {
        this.format = format;
    }


    @Override
    public String getAnalysisName() {
        return program.toString();
    }



    public String getOutputZipFileName() {
        if (getFileExtension(output).endsWith("zip")){
            return output;
        }
        else {
            String newName=output.substring(0,output.lastIndexOf(".")).concat(".zip"); // change file extension to .zip
            return newName;
        }
    }

    public String getOutputFileName() {
            return output;
    }

    public void setOutputFileName(String output) {
                this.output = output;
     }






    public BlastLaunchDTO(String id,BLASTProgram program) {
        super(id);
        this.program = program;
    }

    public BlastLaunchDTO(BLASTProgram program) {
        this(UUID.randomUUID().toString(),program);
    }

    public String asCommand()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("-db ") ;
        sb.append(database);

        sb.append("-query ") ;
        sb.append(query.getAbsolutePath());

        sb.append("-outfmt  ") ;
        sb.append(format);

        sb.append("-out  ") ;
        sb.append(query.getAbsolutePath());

        return sb.toString();
    }

    public BlastLaunchDTO  withNewQuery(File newQuery,String output)  {
        BlastLaunchDTO ret = new BlastLaunchDTO(program);
        ret.database = database;
        ret.format = format;
        ret.query = newQuery;
        ret.output = output;
        return ret;
    }

    public static  String makeOutputName(File in,boolean isXML)  {
        String name = in.getName();
        String base = name.substring(0,name.lastIndexOf('.')) ;
        if(isXML)
             return base + ".xml";
        else
            return base + ".txt";

    }

    public File getLocalJobDirectory() {

        String pathname = "/opt/blastserver";
        if(OSValidator.isWindows() )
               pathname = "C:" + pathname;
        File f =  new File(pathname) ;
        File jobDir = new File(f,id);
        if(!jobDir.exists())   {
            jobDir.mkdirs();
        }
        return jobDir;
    }

    private String getFileExtension(String name) {
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }
}
