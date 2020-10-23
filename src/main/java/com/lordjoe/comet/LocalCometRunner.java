/*
package com.lordjoe.comet;

import com.devdaily.system.SystemCommandExecutor;
import com.lordjoe.locblast.BlastLaunchDTO;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

*/
/**
 * com.lordjoe.comet.LocalCometRunner
 * User: Steve
 * Date: 9/1/20
 *//*

public class LocalCometRunner extends AbstractCometClusterRunner {
      public static File baseDir = new File("/opt/blastserver");

     public final File jobBase;
     public final File input_dir;
     public final File output_dir;
     public final File data;
     public final File params;


    public LocalCometRunner(BlastLaunchDTO job, Map<String, ? extends Object> paramsX) {
        super(job,paramsX);
        jobBase = new File(baseDir, job.id);

        data = new File(paramsX.get("query").toString());
        params = new File(paramsX.get("params").toString());
        if (!jobBase.mkdirs())
            throw new UnsupportedOperationException("Cannot make directory " + jobBase.getAbsolutePath());
        input_dir = new File(jobBase, "input_files");
        if (!input_dir.mkdirs())
            throw new UnsupportedOperationException("Cannot make directory " + input_dir.getAbsolutePath());
        output_dir = new File(jobBase, "output_files");
        if (!output_dir.mkdirs())
            throw new UnsupportedOperationException("Cannot make directory " + output_dir.getAbsolutePath());
        CometUtilities.splitFile(data, input_dir);

    }




    @Override
    protected void cleanUp() {

    }


    public File getInputDirectory() {
        return input_dir;
    }

    public File getOutputDirectory() {
        return output_dir;
    }

    public void runSplitBlastPJob() {
        try {


            File parentFile = data.getParentFile();
            File splitDirectory;

            if (parentFile == null)
                splitDirectory = getInputDirectory();
            else
                splitDirectory = new File(parentFile, job.id);
            if (!splitDirectory.isDirectory() && !splitDirectory.mkdirs())
                throw new UnsupportedOperationException("cannot make directory " + job.id);

            File outSplitDirectory = getOutputDirectory();
            if (!outSplitDirectory.isDirectory() && !outSplitDirectory.mkdirs())
                throw new UnsupportedOperationException("cannot make directory " + outSplitDirectory.getAbsolutePath());

            int numberEntries = CometUtilities.countCometEntities(data);

            CometUtilities.splitFile(data, splitDirectory);

            File[] files = splitDirectory.listFiles();
            List<File> tempFiles = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                tempFiles.add(file);
            }


            for (File tempFile : tempFiles) {
                List<String> commandInformation = buildCommandList(tempFile);
                showCommand(commandInformation);

                SystemCommandExecutor executor = new SystemCommandExecutor(commandInformation);
                int i = executor.executeCommand();
                if (i != 0) {
                    throw new IllegalStateException("command failed " + executor.getStandardErrorFromCommand());
                }

            }
            List<File>  output = getOutputFiles(splitDirectory);
            File outFile = new File(outSplitDirectory,data.getName() + ".pep.xml");
            MergeCometXML.mergeXMLFiles_P(outFile,output);
             //    mergeXMLFiles_P(output , inFiles);

        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
    }


    protected List<File> getOutputFiles(File directory)  {
        List<File> ret = new ArrayList<>();
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".pep.xml");
            }
        }) ;
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                ret.add(files[i]);

            }
        }

        return ret;
    }

    protected void showCommand(List<String> cmd) {
        StringBuilder sb = new StringBuilder();
        for (String s : cmd) {
            sb.append(s);
            sb.append(" ");
        }
        String str = sb.toString().trim();
        System.out.println(str);
    }


    protected List<String> buildCommandList(File f) {
        List<String> ret = new ArrayList<String>();
        ret.add("comet");
        ret.add(f.getAbsolutePath());
           ret.add("-P" + params.getAbsolutePath());

        return ret;
    }

    public static void main(String[] args) {
        run(args);

    }


    public static void run(String[] args) {
        BlastLaunchDTO dto = handleLocBlastArgs(args) ;
        Map<String, ?> params = buildParameters(args);
        LocalCometRunner me = new LocalCometRunner(dto,params);
        me.runSplitBlastPJob();
    }

    @Override
    public void run() {
        runSplitBlastPJob();
    }
}
*/
