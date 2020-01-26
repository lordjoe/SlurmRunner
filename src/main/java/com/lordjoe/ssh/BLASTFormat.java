package com.lordjoe.ssh;

/**
 * com.lordjoe.ssh.BLASTFormat
 * User: Steve
 * Date: 1/25/20
 */
public enum BLASTFormat {
    /*
0 = pairwise,
1 = query-anchored showing identities,
2 = query-anchored no identities,
3 = flat query-anchored, show identities,
4 = flat query-anchored, no identities,
5 = XML Blast output,
6 = tabular,
7 = tabular with comment lines,
8 = Text ASN.1,
9 = Binary ASN.1
10 = Comma-separated values
11 = BLAST archive format (ASN.1)
12 = Seqalign (JSON),
13 = Multiple-file BLAST JSON,
14 = Multiple-file BLAST XML2,
15 = Single-file BLAST JSON,
16 = Single-file BLAST XML2,
17 = Sequence Alignment/Map (SAM),
18 = Organism Report
     */
    Pairwise(0),
    QueryId(1),
    QueryAnchored(2),
    FlatQueryAnchoredId(3),
    FlatQueryAnchored(4),
    XML(5),
    SEQAlign(12),
    MULTI_JSON(13),
    MULTI_XML2(14),
    JSON(15),
    XML2(16),
    SAM(17),
    OrganismReport(18);

    public final int code;

    BLASTFormat(int code) {
        this.code = code;
    }

    public static BLASTFormat fromCode(int cd) {
        BLASTFormat[] values = BLASTFormat.values();
        for (int i = 0; i < values.length; i++) {
            if(values[i].code == cd)
              return values[i];
        }
        return null;
    }
}
