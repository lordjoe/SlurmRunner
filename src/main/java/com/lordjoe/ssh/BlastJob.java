package com.lordjoe.ssh;

import java.io.File;
import java.util.List;

/**
 * com.lordjoe.ssh.BlastJob
 * User: Steve
 * Date: 1/25/20
 */
public class BlastJob {
    public final BlastLaunchDTO dto;
    public final List<File> outputFiles;
    public final  String mergedOutput;

    public BlastJob(BlastLaunchDTO dto, List<File> outputFiles, String mergedOutput) {
        this.dto = dto;
        this.outputFiles = outputFiles;
        this.mergedOutput = mergedOutput;
    }

    public boolean finished() {
        for (File outputFile : outputFiles) {
            if(!outputFile.exists())
                return false;
        }
        return true;
    }

    public double fractionBlastFinished()
    {
        int expected = outputFiles.size();
        if(expected ==0)
            return 1;
        int completed = 0;
        for (File outputFile : outputFiles) {
            if(!outputFile.exists())
                return completed++;
        }
        return completed / expected;
    }
}
