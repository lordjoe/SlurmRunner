package com.lordjoe.locblast;

import java.io.File;
import java.util.UUID;

/**
 * com.lordjoe.ssh.BlastLaunchDTO
 * User: Steve
 * Date: 1/25/20
 */
public class BlastLaunchDTO {
    public final BLASTProgram program;
    public final String id;
    public String database;
    public File query;   // original file
    public BLASTFormat format;
    public String output;




    public BlastLaunchDTO(String id,BLASTProgram program) {
        this.id = id;
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
        File f =  new File("/opt/blastserver") ;
        File jobDir = new File(f,id);
        if(!jobDir.exists())   {
            jobDir.mkdirs();
        }
        return jobDir;
    }
}
