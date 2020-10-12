package com.lordjoe.comet;

import java.io.*;
import java.util.List;

/**
 * com.lordjoe.blast.MergeXML
 * User: Steve
 * Date: 2/9/20
 */
public class MergeCometXML {

    public static final String FOOTER_N =
            " </msms_run_summary>\n" +
                    "</msms_pipeline_analysis>\n";


    public static void writeFile(String fileName, String data) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(new File(fileName)));
            out.println(data);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void mergeXMLFiles_P(File outFile, File[] inFiles) {
          try {
            PrintWriter out = new PrintWriter(new FileWriter(outFile));
            if (inFiles != null && inFiles.length > 0) {
                String header = readHeaderP(inFiles[0]);
                    out.print(header);
                for (int i = 0; i < inFiles.length; i++) {
                    File inFile = inFiles[i];
                       addHits_P(out, inFile );
                }
                out.println(FOOTER_N);
              } else {
                out.println("<!-- No Data!! -->");
            }
            out.close();

        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }

    public static void mergeXMLFiles_P(File outFile, List<File> inFiles) {
         try {
            PrintWriter out = new PrintWriter(new FileWriter(outFile));

            String header = readHeaderP(inFiles.get(0));
            int[] hitnum = new int[1];
            out.print(header);
            for (File inFile : inFiles) {
                addHits_P(out, inFile);

            }
            out.println(FOOTER_N);
            out.close();

        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }

    private static void addHits_P(PrintWriter out, File inFile ) {
        try {
            System.out.println(inFile.getAbsolutePath());
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            int NHeaderLines = 0;
            int NHitLines = 0;
            int NHits = 0;
            int NSpectra = 0;
            int NLines = 0;
            while (line != null && !line.contains("</search_summary>")) {
                NLines++;
                NHeaderLines++;
                line = rdr.readLine();
            }
            line = rdr.readLine();
            while (line != null) {
                NLines++;
                NHitLines++;
                if(line.contains("<spectrum_query"))
                    NSpectra++;
                if(line.contains("<search_hit"))
                    NHits++;

                if (line.startsWith("</msms_run_summary>")) {
                    rdr.close();
                    return;
                }
                out.println(line);
                line = rdr.readLine();
            }
            rdr.close();
            System.out.println("Number Lines = " + NLines);
            System.out.println("Number Spectra = " + NSpectra);
            System.out.println("Number Lines = " + NHits);

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
                sb.append(line);
                sb.append("\n");
                if (line.trim().startsWith("</search_summary>")) {
                    rdr.close();
                    return sb.toString();
                }
                line = rdr.readLine();
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    public static void mergeCometXMLFiles(String[] args) throws IOException, InterruptedException {
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
        mergeXMLFiles_P(outfile , files);

    }

    public static void usage(String[] args) {
        System.out.println("Usage Failure");
        System.out.println("Argument count " + args.length);
        System.out.println("MergeXMLFiles directory_with_files_toMerge mergedfile");
    }

    public static void main(String[] args) throws Exception {
        mergeCometXMLFiles(args);
    }


}
