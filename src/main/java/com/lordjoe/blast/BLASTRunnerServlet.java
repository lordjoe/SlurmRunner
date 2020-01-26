package com.lordjoe.blast;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * com.lordjoe.blast.BLASTRunnerServlet
 * User: Steve
 * Date: 1/17/20
 */
public class BLASTRunnerServlet extends HttpServlet {

    public final LocBlast jobs;

    public BLASTRunnerServlet(LocBlast j) {
        jobs = j;
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        String program = request.getParameter("program"); //   BlastP, BlastN, BlastX, tBlastN...
        GenericBlastParameters params = GenericBlastParameters.getRealParameters(program);
        params.datalib = request.getParameter("datalib"); //  NR, NR/NT, SwissProt, RefSeq-Protein, ...
        params.sequencedata = request.getParameter("sequence");
        params.sequenceFile = request.getParameter("file");
        params.outputFormat = request.getParameter("outfmt");

        int numberAlignments = Integer.parseInt(request.getParameter("bn_num_alignments"));
        double chancematches = Double.parseDouble(request.getParameter("bn_evalue"));
        int wordSize = Integer.parseInt(request.getParameter("bn_word_size"));
        String matchScores = request.getParameter("bn_match_scores");
        String gapCosts = request.getParameter("bn_gapcosts");
        boolean maskQuery =  new Boolean(request.getParameter("bn_filter2"));
// do some processing here...

        switch (program)    {
            case  "blastn":
                BLASTNParameters realN = (BLASTNParameters)params;
                HandleBlastN.handleParams(jobs,realN,request, response);
                break;
            case  "blastp":
                BLASTPParameters realP = (BLASTPParameters)params;
                HandleBlastP.handleParams(jobs,realP,request, response);
                break;
            default:
                throw new UnsupportedOperationException(program + " is not supported");
        }


    }
}
