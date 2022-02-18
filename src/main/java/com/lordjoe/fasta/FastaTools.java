package com.lordjoe.fasta;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * com.lordjoe.fasta.FastaTools
 * User: Steve
 * Date: 11/14/2019
 * When called as a main this is given a fasta file  an output directory and splits the file into 7 parts
 */
public class FastaTools {


    public static List<String> allFastas(File input) {
        try {
            List<String> ret = new ArrayList<>();
            FastaReader fr = new FastaReader(input);
            String fasta = fr.readFastaEntry();
            while (fasta != null) {
                ret.add(fasta);
                fasta = fr.readFastaEntry();
            }
            fr.close();
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public  static List<String> allFastas(List<File> input) {
        List<String> ret = new ArrayList<>();
        for (File file : input) {
            List<String> items = allFastas(file);
            ret.addAll(items);
        }
        return ret;
    }

    /**
     * read a fasta file return the number of fields
     *
     * @param rdr
     * @return
     */
    public static int countFastaEntities(File f) {
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(f));
            return countFastaEntities(rdr);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * read a fasta file return the number of fields
     *
     * @param rdr
     * @return
     */
    public static int countFastaEntities(List<File> input) {
        int ret = 0;
        for (File file : input) {
           ret += countFastaEntities(file);
        }
        return ret;
    }


    /**
     * read a fasta file return the number of fields
     *
     * @param rdr
     * @return
     */
    public static int countFastaEntities(LineNumberReader rdr) {
        try {
            int ret = 0;
            String line = rdr.readLine();
            while (line != null) {
                if (line.startsWith(">"))
                    ret++;
                line = rdr.readLine();
            }
            rdr.close();
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * read a fasta file return the number of fields
     *
     * @param rdr
     * @return
     */
    public static List<String> readFastaHeaders(LineNumberReader rdr) {
        try {
            List<String> ret = new ArrayList<>();
            String line = rdr.readLine();
            while (line != null) {
                if (line.startsWith(">"))
                    ret.add(line.substring(1));
                line = rdr.readLine();
            }
            rdr.close();
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static LineNumberReader getReader(File f) {
        try {
            return new LineNumberReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    public static PrintWriter getWriter(File f) {
        try {
            return new PrintWriter(new FileWriter(f));
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void splitFastaFile(File in, File outDirectory, String baseName, int splitSize, int numberEntries) {
        splitFastaFile(in, outDirectory, baseName, splitSize,numberEntries, Integer.MAX_VALUE);
    }

    public static void splitFastaFile(File in, File outDirectory, String baseName, int splitSize,  int numberEntries,int maxsplits) {
        try {
            long l = in.length();
            LineNumberReader rdr = getReader(in);
            if (!outDirectory.exists()) {
                if (!outDirectory.mkdirs())
                    throw new UnsupportedOperationException("Cannot make output directory " + outDirectory);
            }
            List<String> headers = readFastaHeaders(rdr);
            int numberSplits =   numberEntries/ splitSize;
               rdr = getReader(in);
            String line = rdr.readLine();
            for (int i = 0; i < numberSplits; i++) {
                String index = String.format("%03d", i + 1);
                File outFile = new File(outDirectory, baseName + index + ".faa");
                System.out.println(outFile.getAbsolutePath());
                PrintWriter out = getWriter(outFile);
                if(i + 1 == numberSplits)
                    splitSize = Integer.MAX_VALUE;
                line = writeSplit(out, rdr, line, splitSize);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * @param sourceDirectory holds split FAStA Files
     * @param outputDirectory hold output XML
     * @return SLURM commands to execute
     */
    public String[] generateCommands(File sourceDirectory, File outputDirectory) {
        File[] files = sourceDirectory.listFiles();
        if (files == null)
            return null;     // nothing there
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    private static String writeSplit(PrintWriter out, LineNumberReader rdr, String line, int splitSize) throws IOException {
        int count = 0;
        while (line != null) {
            if (line.startsWith(">")) {
                count++;
                if (count > splitSize) {
                    out.close();
                    return line;
                }
            }
            out.println(line);
            line = rdr.readLine();
        }
        out.close();
        return line;
    }


    public static void splitFiles(String[] args) {
        int index = 0;
        File in = new File(args[index++]);
        File outDirectory = new File(args[index++]);

        int numberEntries = FastaTools.countFastaEntities(in);
        int numberProcessors = 7;
        int splitSize =  (numberEntries / numberProcessors);


          String baseName = "splitFile";
        splitFastaFile(in, outDirectory, baseName, splitSize, numberEntries);
    }

    public static void main(String[] args) {
        splitFiles(args);
    }


}
