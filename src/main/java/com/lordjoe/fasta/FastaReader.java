package com.lordjoe.fasta;

import java.io.*;

/**
 * com.lordjoe.fasta.FastaReader
 * User: Steve
 * Date: 1/24/20
 */
public class FastaReader extends LineNumberReader {

    public static void makeSubFasta(File fasta,File outFile,int elements)    {
        try {
            FastaReader fr = new FastaReader(fasta);
            PrintWriter out = new PrintWriter(new FileWriter(outFile));
            String element = fr.readFastaEntry();
            int written = 0;
            while(element != null)  {
                if(written++ >= elements) {
                    break;
                }
                out.print(element);
                element = fr.readFastaEntry();
            }
            fr.close();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    private String lastLine;
    private boolean readStart;

    public FastaReader(File f) throws FileNotFoundException {
              this(new FileReader(f));
    }
    public FastaReader(Reader reader) {
        super(reader);
    }

    public String getLastLine() {
        try {
            if (!readStart) {
                lastLine = readLine();
                readStart = true;
            }
            return lastLine;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * read one entry - return null when done
     * @return
     */
    public String readFastaEntry() {
        try {
            String ll = getLastLine();
            if (ll == null)
                return null;
            if (!ll.startsWith(">"))
                throw new IllegalStateException("not Fasta does not start >");
            StringBuilder sb = new StringBuilder();
            sb.append(ll);
            sb.append("\n");
            String line = readLine();
            while (line != null) {
                if (line.startsWith(">"))
                    break;
                sb.append(line);
                sb.append("\n");
                 line = readLine();
            }
            lastLine = line;
            return sb.toString(); // at end
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * arg 0 fasta file
     * arg 1 subfile
     * arg2 numberelements
     * @param args
     */
    public static void main(String[] args) {
        File inp = new File(args[0]) ;
        File out = new File(args[1]) ;
        int elements = Integer.parseInt(args[2]);
        makeSubFasta(inp,out,  elements);
    }
}
