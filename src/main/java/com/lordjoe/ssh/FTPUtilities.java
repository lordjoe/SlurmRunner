package com.lordjoe.ssh;

import com.jcraft.jsch.Session;
import com.lordjoe.utilities.FileUtilities;

import java.io.File;
/**
 * com.lordjoe.ssh.FTPUtilities
 * User: Steve
 * Date: 1/26/2020
 */
public class FTPUtilities {

    public  static void createFile(String[] args,ClusterSession  me)  {
          try {
            String command = FileUtilities.readInFile(new File(args[0]));
            me.ftpFileCreate(args[1], command,0666);
            Session session = me.getSession();
            session.disconnect();
              System.out.println("Done");
        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }

    public  static void downloadFile(String[] args,ClusterSession  me)  {
          try {
            String local = args[1];
            String remote = args[0];
            me.ftpFileGet(new File(local), remote);
             Session session = me.getSession();
            session.disconnect();
             System.out.println("Done");
        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }



}
