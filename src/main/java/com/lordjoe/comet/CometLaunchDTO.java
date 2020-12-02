package com.lordjoe.comet;

import com.lordjoe.ssh.LaunchDTO;

import java.io.File;
import java.util.UUID;

/**
 * com.lordjoe.ssh.BlastLaunchDTO
 * User: Steve
 * Date: 1/25/20
 */
public class CometLaunchDTO extends LaunchDTO {
    private boolean databaseIsRemote;
    private String database;


    private File params;   // original file
    private File spectra;   // original file

    public CometLaunchDTO(String id) {
        super(id);
    }

    @Override
    public String getAnalysisName() {
        return "Comet";
    }

    public CometLaunchDTO() {
        this(UUID.randomUUID().toString());
    }


    
    public File getParams() {
        return params;
    }

    public void setParams(File params) {
        this.params = params;
    }


    public boolean isDatabaseIsRemote() {
        return databaseIsRemote;
    }

    public void setDatabaseIsRemote(boolean databaseIsRemote) {
        this.databaseIsRemote = databaseIsRemote;
    }

    public String getJobDatabaseName() {
        return database;
    }

    public void setJobDatabaseName(String database) {
        this.database = database;
    }

    public File getSpectra() {
        return spectra;
    }

    public void setSpectra(File spectra) {
        this.spectra = spectra;
    }
 
    public String getOutputZipFileName() {
        return spectra.getName().replace(".mgf",".pep.xml");
    }



    public String asCommand() {
        StringBuffer sb = new StringBuffer();
        sb.append("-db ");
        sb.append(database);

        sb.append("-query ");
        sb.append(spectra.getAbsolutePath());

        sb.append("-out  ");
        sb.append(spectra.getAbsolutePath());

        return sb.toString();
    }

    public CometLaunchDTO withNewQuery(File newQuery, String output) {
        CometLaunchDTO ret = new CometLaunchDTO();
        ret.database = database;
        ret.spectra = newQuery;
          return ret;
    }

    public static String makeOutputName(File in, boolean isXML) {
        String name = in.getName();
        String base = name.substring(0, name.lastIndexOf('.'));
        if (isXML)
            return base + ".xml";
        else
            return base + ".txt";

    }

    public File getLocalJobDirectory() {
        File f = new File("/opt/blastserver");
        File jobDir = new File(f, id);
        if (!jobDir.exists()) {
            jobDir.mkdirs();
        }
        return jobDir;
    }
}
