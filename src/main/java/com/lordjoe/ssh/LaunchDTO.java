package com.lordjoe.ssh;

/**
 * com.lordjoe.ssh.LaunchDTO
 * User: Steve
 * Date: 10/24/20
 */
public abstract class LaunchDTO {
    public final String id;

    public LaunchDTO(String id) {
        this.id = id;
    }

    public abstract String getAnalysisName();
}
