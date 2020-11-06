package com.lordjoe.blast;

/**
 * com.lordjoe.blast.GenericBlastParameters
 * User: Steve
 * Date: 1/17/20
 */
public class GenericBlastParameters {

    public static GenericBlastParameters getRealParameters(String program)    {
        switch(program)  {
            case "blastn":
                return new BLASTNParameters();
            case "blastp":
                return new BLASTPParameters();
            case "blastx":
                return new BLASTXParameters();
            default:
                throw new UnsupportedOperationException(program + " is not supported");
        }
    }
    public String program ; // request.getParameter("program"); //   BlastP, BlastN, BlastX, tBlastN...
    public String datalib ; // request.getParameter("datalib"); //  NR, NR/NT, SwissProt, RefSeq-Protein, ...
    public String sequencedata ; // request.getParameter("sequence");
    public String sequenceFile ; // request.getParameter("file");
    public String outputFormat ; // request.getParameter("outfmt");

}
