package com.lordjoe.ssh;

/**
 * com.lordjoe.ssh.BLASTProgram
 * User: Steve
 * Date: 1/25/20
 */
public enum BLASTProgram {

     BLASTN,BLASTP,RPBLAST,RPSTBLAST,PSIBLAST,TBLASTN;
    public static BLASTProgram fromString(String s) {
        s = s.toUpperCase();
        return BLASTProgram.valueOf(s);
    }
}
