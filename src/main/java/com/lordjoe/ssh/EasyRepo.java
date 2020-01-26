package com.lordjoe.ssh;

import com.jcraft.jsch.*;

import java.util.Vector;

/**
 * com.lordjoe.ssh.EasyRepo
 * User: Steve
 * Date: 10/23/2019
 */
public class EasyRepo implements HostKeyRepository {

    EasyRepo() {
    }

    public int check(String host, byte[] bkey) {
        return 0;
    }

    public void add(HostKey hostkey, UserInfo info) {
    }

    public void remove(String host, String type) {
    }

    public void remove(String host, String type, byte[] bkey) {
    }

    public String getKnownHostsRepositoryID() {
        return "";
    }

    public HostKey[] getHostKey() {
        return new HostKey[0];
    }

    public HostKey[] getHostKey(String host, String type) {
        return new HostKey[0];
    }

    public String toString() {
        return "EasyRepo()";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else {
            return o instanceof EasyRepo;
        }
    }

    public int hashCode() {
        int result = 1;
        return result;
    }

}
