package com.lordjoe.comet;

import com.devdaily.system.SystemCommandExecutor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * com.lordjoe.comet.LocalCometRunner
 * User: Steve
 * Date: 9/1/20
 */
public class LocalCometRunner implements Runnable {
    public static final LocalCometRunner[] EMPTY_ARRAY = {};
    public static File baseDir = new File("/opt/blastserver");

    public final String id;
    public final File jobBase;
    public final File input_dir;
    public final File output_dir;
    public final File data;
    public final File params;


    public LocalCometRunner(String uid, File query, File params) {
        id = uid;
        jobBase = new File(baseDir, id);
        this.params = params;
        this.data = query;

        if (!jobBase.mkdirs())
            throw new UnsupportedOperationException("Cannot make directory " + jobBase.getAbsolutePath());
        input_dir = new File(jobBase, "input_files");
        if (!input_dir.mkdirs())
            throw new UnsupportedOperationException("Cannot make directory " + input_dir.getAbsolutePath());
        output_dir = new File(jobBase, "output_files");
        if (!output_dir.mkdirs())
            throw new UnsupportedOperationException("Cannot make directory " + output_dir.getAbsolutePath());
        CometUtilities.splitFile(query, input_dir);

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
                splitDirectory = new File(parentFile, id);
            if (!splitDirectory.isDirectory() && !splitDirectory.mkdirs())
                throw new UnsupportedOperationException("cannot make directory " + id);

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
        int index = 0;
        File query = new File(args[index++]);
        File params = new File(args[index++]);
        File output = new File(args[index++]);
        String id = UUID.randomUUID().toString();

        LocalCometRunner me = new LocalCometRunner(id, query, params);
        me.runSplitBlastPJob();

    }


    @Override
    public void run() {
        runSplitBlastPJob();
    }
}
