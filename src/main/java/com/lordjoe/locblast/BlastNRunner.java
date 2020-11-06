package com.lordjoe.locblast;

import com.lordjoe.ssh.ClusterLauncher;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * com.lordjoe.ssh.BlastNRunner
 * User: Steve
 * Date: 1/25/20
 */
public class BlastNRunner extends BlastRunnerTemplate {


    public static BlastLaunchDTO fromArgs(String[] args) {
        int index = 0;
        BlastLaunchDTO ret = new BlastLaunchDTO(BLASTProgram.BLASTN);
        requireString(args[index],"-db",index , args);
        index++;
        
        ret.setJobDatabaseName(args[index++]);
        requireString(args[index],"-query",index , args);
        index++;

        ret.setQuery(new File(args[index++]));
        requireString(args[index],"-outfmt",index , args);
        index++;

        requireString(args[index],"16",index , args);
        ret.setBLASTFormat(BLASTFormat.XML2);
        index++;

        requireString(args[index],"-out",index , args);
        index++;
        ret.setQuery( new File(args[index++]));

        return ret;
    }
    public BlastNRunner(String id, Map<String, ?> param) {
        super(id, param);
    }

    private static void runLocally(BlastLaunchDTO dto) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    private static void runOnCluster(BlastLaunchDTO dto, ClusterLauncher cl) {

        File tempDir = new File(dto.id);
        List<File> files = cl.buildFileSplit(tempDir);
        List<File> outfiles = makeOutputFiles(files);



    }

    public static void main(String[] args) {
        BlastLaunchDTO dto =  fromArgs(args) ;
        ClusterLauncher cl = new ClusterLauncher(dto.getQuery());
        if(cl.isLocalRun())  {
            runLocally(dto);
        }
        else {
            runOnCluster(dto,cl) ;
        }

    }


}
