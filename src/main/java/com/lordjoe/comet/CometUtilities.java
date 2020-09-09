package com.lordjoe.comet;

import java.io.*;

/**
 * com.lordjoe.comet.CometUtilities
 * User: Steve
 * Date: 9/7/20
 */
public class CometUtilities {

    private static LineNumberReader getReader(File f) {
        try {
            return new LineNumberReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    private static PrintWriter getWriter(File f) {
        try {
            return new PrintWriter(new FileWriter(f));
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
    public static int countCometEntities(File in) {
        String name = in.getName().toLowerCase();
        if(name.endsWith(".mgf"))  {
            int numberEntries = countMGFEntities(in);
             return numberEntries;
        }
        throw new UnsupportedOperationException("Cannot split files named " + in.getName());

    }

    /**
     * read a fasta file return the number of fields
     *
     * @param rdr
     * @return
     */
    public static int countMGFEntities(File in) {
        try {
           return countMGFEntities(new LineNumberReader(new FileReader(in)));
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
    public static int countMGFEntities(LineNumberReader rdr) {
        try {
            int ret = 0;
            String line = rdr.readLine();
            while (line != null) {
                if (line.contains("BEGIN IONS"))
                    ret++;
                line = rdr.readLine();
            }
            rdr.close();
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }



    public static void splitMGFFile(File in, File outDirectory, String baseName, int splitSize, int numberEntries) {
        splitMGFFile(in, outDirectory, baseName, splitSize,numberEntries, Integer.MAX_VALUE);
    }

    public static void splitMGFFile(File in, File outDirectory, String baseName, int splitSize,  int numberEntries,int maxsplits) {
        try {
            LineNumberReader rdr = getReader(in);
            if (!outDirectory.exists()) {
                if (!outDirectory.mkdirs())
                    throw new UnsupportedOperationException("Cannot make output directory " + outDirectory);
            }
            int numberSplits =   numberEntries/ splitSize;
            rdr = getReader(in);
            String line = rdr.readLine();
            for (int i = 0; i < numberSplits; i++) {
                String index = String.format("%03d", i + 1);
                File outFile = new File(outDirectory, baseName + index + ".mgf");
                System.out.println(outFile.getAbsolutePath());
                PrintWriter out = getWriter(outFile);
                if(i + 1 == numberSplits)
                    splitSize = Integer.MAX_VALUE;
                line = writeMGFSplit(out, rdr, line, splitSize);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    private static String writeMGFSplit(PrintWriter out, LineNumberReader rdr, String line, int splitSize) throws IOException {
        int count = 0;
        while (line != null) {
            if (line.contains("BEGIN IONS"))  {
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

        int numberEntries = countMGFEntities(in);
        int splitSize =  (numberEntries / 7);


        String baseName = "splitFile";
        splitMGFFile(in, outDirectory, baseName, splitSize, numberEntries);
    }

    public static void splitFile(File in,File outDirectory)  {
        String name = in.getName().toLowerCase();
        if(name.endsWith(".mgf"))  {
            int numberEntries = countMGFEntities(in);
            int splitSize =  (numberEntries / 7);


            String baseName = "splitFile";
            splitMGFFile(in, outDirectory, baseName, splitSize, numberEntries);
            return;
        }
        throw new UnsupportedOperationException("Cannot split files named " + in.getName());
    }

    public static void main(String[] args) {
        splitFiles(args);
    }

}
