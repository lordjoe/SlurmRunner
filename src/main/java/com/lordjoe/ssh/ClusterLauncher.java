package com.lordjoe.ssh;

import com.lordjoe.fasta.FastaReader;
import com.lordjoe.fasta.FastaTools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * com.lordjoe.ssh.ClusterLauncher
 * User: Steve
 * Date: 1/24/20
 */
public class ClusterLauncher {
    public static final ClusterLauncher[] EMPTY_ARRAY = {};

    public static final String MAXLocalSequencesProp = "MAXLocalSequences";
    public static final String MinSequencesPerMachineProp = "MinSequencesPerMachine";
    public static final String MaxMachinesToRequisitionProp = "MaxMachinesToRequisition";


    public static final Properties clusterProperties = readClusterProperties();
    public static final int MAXLocalSequences = Integer.parseInt(clusterProperties.getProperty(MAXLocalSequencesProp));
    public static final int MinSequencesPerMachine = Integer.parseInt(clusterProperties.getProperty(MinSequencesPerMachineProp));
    public static final int MaxMachinesToRequisition = Integer.parseInt(clusterProperties.getProperty(MaxMachinesToRequisitionProp));

    private static Properties readClusterProperties() {
        try {
            InputStream is = ClusterLauncher.class.getResourceAsStream("/com/lordjoe/blast/ClusterLaunchAsterix.properties");
            Properties cp = new Properties();
            cp.load(is);
            return cp;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public final File fastaFile;
    public int numberSequences;
    public static int commandNumberProcessors = 0;

    public ClusterLauncher(File f) {
        fastaFile = f;
        numberSequences = FastaTools.countFastaEntities(f);
    }

    public static int getNumberProcessors() {
        if(commandNumberProcessors > 0)
            return commandNumberProcessors;
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public static void setCommandNumberProcessors(int commandNumberProcessorsX) {
         commandNumberProcessors = commandNumberProcessorsX;
    }

    public boolean isLocalRun() {
        return numberSequences <= MAXLocalSequences;
    }

    public int getNumberRemoteMachines() {
        int possibleMachines = 1 + numberSequences / MinSequencesPerMachine;
        return Math.min(MaxMachinesToRequisition, possibleMachines);
    }

    public List<File> buildFileSplit(File targetDirectory ) {
        try {
            List<File> ret = new ArrayList<>();
            if (isLocalRun())
                return ret;
            int nFiles = getNumberRemoteMachines();
            if (nFiles == 1) {
                ret.add(fastaFile);
            }
            int sequencesPerFile = numberSequences / nFiles;
            int remainder = numberSequences % nFiles;
            FastaReader rdr = new FastaReader(fastaFile);
            String fasta = rdr.readFastaEntry();
            for (int i = 0; i < nFiles; i++) {
                int toRead = sequencesPerFile;
                if(i == 0)
                    toRead += remainder;
                    File outFile = new File(targetDirectory, "query" + "_" + String.format("%02d" , i) + ".fas");
                ret.add(outFile);
                PrintWriter out = new PrintWriter(new FileWriter(outFile));

                fasta = populateOutfile(out, rdr, toRead, fasta);

            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    private String populateOutfile(PrintWriter out, FastaReader rdr, int sequencesPerFile, String fasta) {
        int added = 0;

        while (fasta != null) {
            if (added >= sequencesPerFile) {
                out.close();
                return fasta;
            }
            out.print(fasta);
            added++;
            fasta = rdr.readFastaEntry();
        }
        out.close();
        return fasta;
    }

    public static void main(String[] args) {
        ClusterLauncher me = new ClusterLauncher(new File(args[0]));
        System.out.println(me.numberSequences + " sequences");
        System.out.println("Needed machines " + me.getNumberRemoteMachines());
        File outDir = new File("splits");
        outDir.mkdirs();
        List<File> files = me.buildFileSplit(outDir);
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
        }
    }


}
