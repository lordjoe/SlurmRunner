package com.lordjoe.ssh;

/**
 * com.lordjoe.ssh.JobState
 * User: Steve
 * Date: 4/21/2020
 */
public enum JobState {
    NullState(null,0),


    JobFinished(NullState,12),
    NotificationSent(JobFinished,11),
    FilesCleanedUp(NotificationSent,10),
    OututDownloaded(FilesCleanedUp,9),
    FilesMerged(OututDownloaded,8),
    BlastFinished(FilesMerged,7),
    BlastCalled(BlastFinished,6),
    ScriptsWritten(BlastCalled,5),
    InputUploaded(ScriptsWritten,4),
    InputSplit(InputUploaded,3),
    JarGuaranteed(InputSplit,2),
    RunStarted(JarGuaranteed,1),
    WaitingToStart(RunStarted,0),
    CometCalled(BlastFinished,13),

    Failed(NullState,0);

    JobState(JobState next,int pos) {
        this.next = next;
        position = pos;
    }

    public final JobState next;
    public final int position;

    public static boolean lessOrEqual(JobState j1, JobState j2) {
        return j1.position <= j2.position;
    }
    public static boolean lessOrEqual(String j1, JobState j2) {
        if(j1 == null)
            return false;
        JobState j1x = valueOf(j1);
        return lessOrEqual(j1x,j2);

    }



}
