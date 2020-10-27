package com.lordjoe.blast;


import com.lordjoe.utilities.FileUtilities;

import java.io.*;

import static com.lordjoe.utilities.FileZipper.zipFile;

/**
 * com.lordjoe.blast.MergeXML
 * User: Steve, Simone
 * Date: 2/9/20
 */
public class MergeXML {

    public static final String FOOTER_N =
            "</BlastOutput_iterations>\n" +
                    "</BlastOutput>\n";
    public static final String FOOTER_P =
            "</BlastXML2>\n";


    public static void writeFile(String fileName, String data) {
        File file = new File(fileName);
        writeFile(file, data);
    }

    public static void writeFile(File file, String data) {

        try {
            PrintWriter out = new PrintWriter(new FileWriter(file));
            out.println(data);

            file.setReadable(true, true);
            file.setWritable(true, true);
            out.close();
            //zipIt(file, new File("output.zip"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void mergeXMLFiles_P(String outFilename, File[] inFiles) {
        //this method will merge the cluster jobs results in a file with already a .zip extension
        // and will call a method to actually zip the file
        //This method is not called anywhere in the code, but called via a .jar call in the cluster
        //by the merge script

        File outF = new File(outFilename);
        StringBuilder sb = new StringBuilder();
        sb.append(outFilename);
        sb.append("\n");
        try {
            PrintWriter out = new PrintWriter(new FileWriter(outF));
            if (inFiles != null && inFiles.length > 0) {
                String[] footerArray = { " "};
                String header = readHeaderP(inFiles[0],footerArray);
                int[] hitnum = new int[1];
                out.print(header);
                for (int i = 0; i < inFiles.length; i++) {
                    File inFile = inFiles[i];
                    sb.append(inFile.getAbsolutePath() + "\n");
                    addHits_P(out, inFile, hitnum);
                }
                out.println(FOOTER_P);
            } else {
                out.println("<!-- No Data!! -->");
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

    private static void addHits_P(PrintWriter out, File inFile, int[] hitnum) {
        try {
            System.out.println(inFile.getAbsolutePath());
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            while (line != null && !line.startsWith("<BlastOutput2>"))
                line = rdr.readLine();
            while (line != null) {
                if (line.startsWith("</Iteration_hits>")) {
                    rdr.close();
                    return;
                }
                out.println(line);
                line = rdr.readLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    private static String readHeaderP(File inFile, String[] footerArray) {
        try {
            StringBuilder footer = new StringBuilder();
            StringBuilder sb = new StringBuilder();
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            while (line != null) {
                if (line.trim().startsWith("<BlastOutput2>")) {
                    rdr.close();
                    return sb.toString();
                }
                sb.append(line);
                sb.append("\n");
                line = rdr.readLine();
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void mergeXMLFiles(String[] args) throws IOException, InterruptedException {
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
        mergeXMLFiles_P(outfile.getCanonicalPath(), files);

    }

    public static void usage(String[] args) {
        System.out.println("Usage Failure");
        System.out.println("Argument count " + args.length);
        System.out.println("MergeXMLFiles directory_with_files_toMerge mergedfile");
    }

    public static void main(String[] args) throws Exception {
        mergeXMLFiles(args);
    }


}
