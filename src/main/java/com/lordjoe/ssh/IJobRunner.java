package com.lordjoe.ssh;

import com.lordjoe.locblast.BlastLaunchDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * com.lordjoe.ssh.IJobRunner
 * User: Steve
 * Date: 4/28/2020
 */
public interface IJobRunner extends Runnable {
    public JobState getState();

    public String getId();

    public BlastLaunchDTO getJob();

    public void setLastState(JobState s);

    public JobState getLastState();

    public  Map<String,? extends Object>  filterProperties(Map<String,? extends Object>  in);

    public static Map<String,IJobRunner> byID = new HashMap<>();

    public static void registerRunner(IJobRunner runner) {
        byID.put(runner.getId(),runner);

    }
    public static IJobRunner fromID(String id) {
        return byID.get(id);
    }
}
