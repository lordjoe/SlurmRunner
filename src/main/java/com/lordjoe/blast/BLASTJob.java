package com.lordjoe.blast;

import java.util.UUID;

/**
 * com.lordjoe.blast.BLASTJob
 * User: Steve
 * Date: 1/17/20
 */
public class BLASTJob {

    public final GenericBlastParameters parameters;
    public final UUID id = UUID.randomUUID();

    public BLASTJob( GenericBlastParameters   parameters) {
         this.parameters = parameters;
    }

    public double completeFraction() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public boolean isDone() {
        return completeFraction() >= 1;
    }

    public String XMLText() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

}
