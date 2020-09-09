package com.lordjoe.ssh;

import com.jcraft.jsch.SftpException;
import com.lordjoe.blast.*;
import com.lordjoe.fasta.FastaTools;
import com.lordjoe.utilities.FileUtilities;

import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * com.lordjoe.ssh.JobRunnerUtilities
 * User: Steve
 * Date: 5/3/2020
 */
public class JobRunnerUtilities {


	private SftpException createdToForceClassLoad;
    public static final int LOCAL_LIMIT = 90;
    public static final boolean RUN_LOCALLY = true;

    public static IJobRunner createRunner(Map<String,? extends Object> parameters) {
        String userDir = System.getProperty("user.dir");
        IJobRunner ret = null;
        String id = (String)parameters.get("JobId");
        if(id == null)
            id = UUID.randomUUID().toString();
        String program = (String)parameters.get("program"); //   BlastP, BlastN, BlastX, tBlastN...
        if("comet".equalsIgnoreCase(program))    {
            
        }
 //       SlurmClusterRunner.logMessage("Program is " + program );
        GenericBlastParameters params = GenericBlastParameters.getRealParameters(program);
        params.datalib = (String)parameters.get("datalib"); //  NR, NR/NT, SwissProt, RefSeq-Protein, ...
        params.sequencedata = (String)parameters.get("sequence");
        if (params.sequencedata != null && params.sequencedata.length() > 10) {
            params.sequenceFile = generateSequenceFile(params.sequencedata);
        } else {
            params.sequenceFile = (String)parameters.get("seqfile");
        }
        params.outputFormat = (String)parameters.get("outfmt");


        BlastLaunchDTO dto = new BlastLaunchDTO(id,BLASTProgram.fromString(program));

        dto.database = params.datalib;
        File defaultJobDirectory = dto.getLocalJobDirectory();
        dto.query = new File(defaultJobDirectory,params.sequenceFile);
        dto.format = BLASTFormat.fromCode(Integer.parseInt(params.outputFormat));
        if(dto.format == BLASTFormat.XML2 || dto.format == BLASTFormat.XML ) {
            dto.output = makeXMLFileName( dto.query).getName();
        }
        else {
            dto.output = makeTxtFileName( dto.query).getName();

        }

        int numberFasta = FastaTools.countFastaEntities( dto.query);
        if( RUN_LOCALLY || numberFasta <= LOCAL_LIMIT) {
             ret = new SlurmLocalRunner(dto,parameters);
         }
        else  {
            ret = new SlurmClusterRunner(dto,parameters);
        }

        if(true)
            return ret;


        int numberAlignments = Integer.parseInt((String)parameters.get("bn_max_target_seqs"));
        double chancematches = Double.parseDouble((String)parameters.get("bn_evalue"));
        int wordSize = Integer.parseInt((String)parameters.get("bn_word_size"));
        String matchScores = (String)parameters.get("bn_match_scores");
        String gapCosts = (String)parameters.get("bn_gapcosts");
        boolean maskQuery = new Boolean((String)parameters.get("bn_filter2"));
// do some processing here...

        switch (program) {
            case "blastn":
                BLASTNParameters realN = (BLASTNParameters) params;
                //            HandleBlastN.handleParams(jobs,realN,request, response);
                break;
            case "blastp":
                BLASTPParameters realP = (BLASTPParameters) params;
                //           HandleBlastP.handleParams(jobs,realP,request, response);
                break;
            default:
                throw new UnsupportedOperationException(program + " is not supported");
        }
        return new MockJobRunner(dto);
    }

    private static String generateSequenceFile(String sequencedata) {
        File f = makeSequenceFileName();
        String path = f.getAbsolutePath();
        FileUtilities.writeFile(f, sequencedata);
        return f.getAbsolutePath();
    }

    private static File makeSequenceFileName() {
        int index = 1;
        File test = new File("sequence" + index++ + ".faa");
        while (test.exists()) {
            test = new File("sequence" + index++ + ".faa");
        }
        return test;
    }


    private static File makeXMLFileName(File f) {
        File parent = f.getParentFile();
        String name = f.getName();
        name = name.substring(0, name.lastIndexOf("."));
        if (parent != null) {
            return new File(parent, name +  ".xml");
        }
        File test = new File(name + ".xml");
        return test;
    }

      private static File makeTxtFileName(File f) {
        File parent = f.getParentFile();
        String name = f.getName();
        name = name.substring(0, name.lastIndexOf("."));
        if (parent != null) {
           return new File(parent, name +  ".txt");
        }
        File test = new File(name + ".txt");
        return test;
    }



    public static void main(String[] args) {
        Map<String, String> map = BLASTRunnerServlet.readParameters(new File(args[0]));
        IJobRunner runner = createRunner(map)  ;
        runner.run();
    }

}