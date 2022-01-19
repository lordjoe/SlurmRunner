package com.lordjoe.ssh;

/**
 * com.lordjoe.ssh.SShtester
 * User: Steve
 * Date: 1/17/2022
 */


import com.jcraft.jsch.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * based on Jsch. tested with real VM with Key/password cases. Run ssh. This is
 * a basic one that works with password. If needed we may replace this
 * SshProvider after defining an interface.
 *
 * @author Yuanteng (Jeff) Pei
 *
 */
/**
 * net.schmizz.sshj.examples.lxpSample2
 * User: Steve
 * Date: 1/17/2022
 */
public class SShtester {

    /** The logger. */



    /** The target host. */
    private String targetHost = "login.lxp.lu";

    private String user = "u100019";
    String passphrase = "Carbon12";
    int port = 8822;
    int timeout = 10000;
    String privateKeyPath = "id_ed25519_mlux";



    /**
     * Instantiates a new ssh provider.
     */
    public SShtester() {
    }

    /** The session. */
    private Session session = null;

    /** The channel. */
    private Channel channel = null;

    /**
     * finally: will close the connection.
     *
     * @return the response on singe request
     */
    public String executeSshCommand(String command) {
        String sshResponse = null;

        try {
            session = startSshSessionAndObtainSession();
            channel = sessionConnectGenerateChannel(command,session);
            sshResponse = executeAndGenResponse( (ChannelExec) channel);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (session != null)
                session.disconnect();
            if (channel != null)
                channel.disconnect();
        }

        return sshResponse;

    }

    /**
     * Start ssh session and obtain session.
     *
     * @return the session
     */
    public Session startSshSessionAndObtainSession() {
        Session session = null;
        try {

            JSch jsch = new JSch();
            if (true) {

                String workingDir = System.getProperty("user.home");
                String privKeyAbsPath = workingDir + "/.ssh/"
                        + privateKeyPath;



                if (passphrase != null) {
                    jsch.addIdentity(privKeyAbsPath, passphrase);
                } else {
                    jsch.addIdentity(privKeyAbsPath);
                }
            }

            session = jsch.getSession(user, targetHost,
                   port);


            session.setConfig("StrictHostKeyChecking", "no");
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
        return session;
    }

    /**
     * Session connect generate channel.
     *
     * @param session
     *            the session
     * @return the channel
     * @throws JSchException
     *             the j sch exception
     */
    public Channel sessionConnectGenerateChannel(String command,Session session)
            throws JSchException {
        // set timeout
        session.connect(timeout);

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        // if run as super user, assuming the input stream expecting a password
        if (true) {
            try {
                channel.setInputStream(null, true);

                OutputStream out = channel.getOutputStream();
                channel.setOutputStream(System.out, true);
                channel.setExtOutputStream(System.err, true);
                channel.setPty(true);
                channel.connect();

                out.write((passphrase+"\n").getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            channel.setInputStream(null);
            channel.connect();
        }

        return channel;

    }

    /**
     * Seems there are bad naming in the library the sysout is in
     * channel.getInputStream(); the syserr is in
     * ((ChannelExec)channel).setErrStream(os);
     *
     * @param channel
     *            the channel
     * @return the response on singe request
     */
    public String executeAndGenResponse(ChannelExec channel) {
        String error = null;
        InputStream in = null;
        OutputStream outputStreamStdErr = new ByteArrayOutputStream();
        StringBuilder sbStdOut = new StringBuilder();
        try {

            in = channel.getInputStream();
            channel.setErrStream(outputStreamStdErr);

            byte[] tmp = new byte[20000];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 20000);
                    if (i < 0)
                        break;
                    sbStdOut.append(new String(tmp, 0, i));

                }

                if (channel.isClosed()) {
                    if (in.available() > 0)
                        continue;

                    // exit 0 is good
                    int exitStatus = channel.getExitStatus();
                      break;
                }

                Thread.sleep(2000);
            }

           String response = sbStdOut.toString();
            error =outputStreamStdErr.toString();
            return response;
        } catch (Exception t) {
            throw new RuntimeException(t);
        }


    }



 

    /**
     * Gets the target host.
     *
     * @return the target host
     */
    public String getTargetHost() {
        return targetHost;
    }

    /**
     * Sets the target host.
     *
     * @param targetHost
     *            the new target host
     */
    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public List<String> getFilesInDirectory(ChannelSftp c,String directory)
    {
        List<String> ret = new ArrayList<>();
        try {
             Vector ls = c.ls(directory);
            for (Object l : ls) {
                String filename = ((ChannelSftp.LsEntry) l).getFilename();
                if("..".equals(filename))
                    continue;
                if(".".equals(filename))
                    continue;
                ret.add(filename );
            }
            return ret;
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }

    }


    public static void main(String[] args) throws Exception {
        int timeout = 100000;
        SShtester me = new SShtester();
        Session session = me.startSshSessionAndObtainSession();
        if (!session.isConnected()) {
            session.connect(timeout);
            //               SlurmClusterRunner.logMessage("Session Connected");
        }
        Channel channel = session.openChannel("sftp");
        if (!channel.isConnected()) {
            channel.connect(timeout);
            //               SlurmClusterRunner.logMessage("Channel Connected");
        }
        ChannelSftp c = (ChannelSftp) channel;
        List<String> files = me.getFilesInDirectory( c,"/home/users/u100019");
        String s = me.executeSshCommand("pwd");
        System.out.println(s);
    }

}