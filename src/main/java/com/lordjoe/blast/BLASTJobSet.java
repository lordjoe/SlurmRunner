package com.lordjoe.blast;

import java.util.*;

/**
 * com.lordjoe.blast.BLASTJobSet
 * User: Steve
 * Date: 1/17/20
 */
public class BLASTJobSet {

    private final Map<UUID,BLASTJob> jobs = new HashMap<>();

    public void addJob(BLASTJob job)   {
        jobs.put(job.id,job);
    }
    public void removeJob(BLASTJob job)   {
        jobs.remove(job.id,job);
    }

    public List<BLASTJob> getJobs() {
        return new ArrayList<BLASTJob>(jobs.values());
    }

}
