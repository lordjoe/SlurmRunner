package com.lordjoe.blast;

import com.lordjoe.utilities.FileUtilities;

import java.io.*;

import static com.lordjoe.utilities.FileZipper.zipFile;

/**
 * com.lordjoe.blast.MergeTXT
 * User: Steve
 * Date: 2/9/20
 */
public class MergeTXT {


    public static void writeFile(String fileName, String data) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(new File(fileName)));
            out.println(data);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void mergeTXTFiles(String outFile, File[] inFiles) {
        File outF = new File(outFile);
        try {
            PrintWriter out = new PrintWriter(new FileWriter(outF));
            if (inFiles != null && inFiles.length > 0) {
                for (int i = 0; i < inFiles.length; i++) {
                    File inFile = inFiles[i];
                    LineNumberReader rdr = new LineNumberReader((new FileReader(inFile)));
                    String line = rdr.readLine();
                    while (line != null) {
                        out.println(line);
                        line = rdr.readLine();
                    }
                }
            }
            out.close();
            File parent = outF.getParentFile();
            if (parent != null) {
                zipFile(outF.getAbsolutePath());
            }
            FileUtilities.setReadWritePermissions(outF);
        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }


    public static void mergeTXTFiles(String[] args) throws IOException, InterruptedException {
        int index = 0;
        if (args.length < 2) {
            usage(args);
            return;
        }
        File inDirectory = new File(args[index++]);

        if (!inDirectory.exists() || !inDirectory.isDirectory()) {
            usage(args);
            return;
        }
        File outfile = new File(args[index++]);
        File[] files = inDirectory.listFiles();
        if (files == null) {
            usage(args);
            return;
        }
        mergeTXTFiles(outfile.getCanonicalPath(), files);
    }

    public static void usage(String[] args) {
        System.out.println("Usage Failure");
        System.out.println("Argument count " + args.length);
        System.out.println("MergeXMLFiles directory_with_files_toMerge mergedfile");
    }

    public static void main(String[] args) throws Exception {
        mergeTXTFiles(args);
    }


}
