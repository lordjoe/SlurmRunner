package com.lordjoe.blast;

import java.io.*;

/**
 * com.lordjoe.blast.MergeXML
 * User: Steve
 * Date: 2/9/20
 */
public class MergeXML {

    public static final String FOOTER_N =
            "</BlastOutput_iterations>\n" +
                    "</BlastOutput>\n";
    public static final String FOOTER_P =
            "</BlastXML2>\n";

    
    public static void writeFile(String fileName, String data)    {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(new File(fileName)));
            out.println(data);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
    public static void mergeXMLFiles_P(String outFile, File[] inFiles ) {
        StringBuilder sb = new StringBuilder();
        sb.append(outFile);
        sb.append("\n");
        writeFile("mergefile.txt",sb.toString());
        try {
            PrintWriter out = new PrintWriter(new FileWriter(outFile));
            if(inFiles != null && inFiles.length > 0) {
                String header = readHeaderP(inFiles[0]);
                int[] hitnum = new int[1];
                out.print(header);
                for (int i = 0; i < inFiles.length; i++) {
                    File inFile = inFiles[i];
                    sb.append(inFile.getAbsolutePath() + "\n");
                    addHits_P(out, inFile, hitnum);
                }
                out.println(FOOTER_P);
                 writeFile("mergefilemade.txt", sb.toString());
            }
            else {
               out.println("<!-- No Data!! -->");
            }
            out.close();

        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }

    private static void addHits_N(PrintWriter out, File inFile,int[] hitnum) {
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            while(line != null && !line.startsWith("<Iteration>"))
                line = rdr.readLine();
            while (line != null) {
                if(line.startsWith("  <Iteration_iter-num>") ) {
                    hitnum[0]++;
                    line =  "  <Iteration_iter-num>" +   hitnum[0] +   "</Iteration_iter-num>";
                }
                if( line.startsWith("  <Iteration_query-ID>Query_") )  {
                    line =  "  <Iteration_query-ID>Query_" +   hitnum[0] +   "</Iteration_query-ID>";
                }
                if (line.startsWith("</BlastOutput_iterations>")) {
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

    private static void addHits_P(PrintWriter out, File inFile,int[] hitnum) {
        try {
            System.out.println(inFile.getAbsolutePath());
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            while(line != null && !line.startsWith("<BlastOutput2>"))
                line = rdr.readLine();
            while (line != null) {
                if (line.startsWith("</BlastXML2>")) {
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

    private static String readHeaderN(File inFile) {
        try {
            StringBuilder sb = new StringBuilder();
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            while (line != null) {
                if (line.trim().startsWith("<Iteration>")) {
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
    private static String readHeaderP(File inFile) {
        try {
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
        if(args.length < 2)  {
            usage(args);
            return;
        }
        File inDirectory = new File(args[index++]);

        if(!inDirectory.exists() || !inDirectory.isDirectory()) {
            usage(args);
            return;
        }
        File outfile = new File(args[index++]);
        File[] files = inDirectory.listFiles();
        if (files == null) {
            usage(args);
            return;
        }
        mergeXMLFiles_P(outfile.getCanonicalPath(),files );

    }

    public static void usage(String[] args)
    {
        System.out.println("Usage Failure");
        System.out.println("Argument count " + args.length);
        System.out.println("MergeXMLFiles directory_with_files_toMerge mergedfile");
    }

    public static void main(String[] args) throws  Exception {
            mergeXMLFiles(args);
    }



}
