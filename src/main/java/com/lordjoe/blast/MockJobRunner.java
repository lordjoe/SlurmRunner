package com.lordjoe.blast;

import com.lordjoe.ssh.BlastLaunchDTO;
import com.lordjoe.ssh.IJobRunner;
import com.lordjoe.ssh.JobState;

import java.util.Map;
import java.util.Random;

/**
 * com.lordjoe.blast.MockJobRunner
 * used to test JobState.jsp
 * Every few times getState is called it moves to the next state
 * User: Steve
 * Date: 4/28/2020
 */
public class MockJobRunner implements IJobRunner {
    public static final MockJobRunner[] EMPTY_ARRAY = {};

    public static final Random RND = new Random();
    private JobState current = JobState.RunStarted;
    private JobState lastState = JobState.NullState;
    public final BlastLaunchDTO job;

    public MockJobRunner(BlastLaunchDTO job) {
        this.job = job;
        IJobRunner.registerRunner(this);
    }

    public JobState getState() {
        if (current != JobState.JobFinished  ){
             if (RND.nextInt(100) > 30) {
                current = current.next;
            }
        }
        else  {
            current = JobState.JobFinished;
            lastState= current;
        }
        return current;
    }

    @Override
    public void setLastState(JobState s) {
        lastState = s;
    }

    @Override
    public JobState getLastState() {
        return lastState;
    }

    @Override
    public String getId() {
        return getJob().id;
    }

    @Override
    public BlastLaunchDTO getJob() {
        return job;
    }

    public Map<String,? extends Object> filterProperties(Map<String,? extends Object>  in) {
        return in;
    }


    @Override
    public void run() {
        try {
            while(getState() != current)
                Thread.currentThread().sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
    }
}
