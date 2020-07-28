package com.lordjoe.ssh;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * com.lordjoe.ssh.Gateway
 * User: Steve
 * Date: 7/6/20
 */
public class Gateway {


    public static void main(String[] args) {
        ProxySSH p = null;
        Gateway t = new Gateway();
        try {
            t.go();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Session configureSession(String user,String host,int sshPort,  File keyFile, int localPort, int remotePort) {
        try {
            JSch jsch = new JSch();
            JSch.setLogger(new SSHLogger());
            //configure the tunnel
            System.out.println("Configuring SSH tunnel");
            Session session = jsch.getSession(
                     user,
                     host,
                     sshPort
            );

            jsch.addIdentity( keyFile.getPath() );

            final Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("ConnectionAttempts", "3");
            //dont set the keep alive too low or you might suffer from a breaking connection during start up (for whatever reason)
            session.setServerAliveInterval(1000);//milliseconds
            session.setConfig(config);

            //forward the port
            final int assignedPort = session.setPortForwardingL(
                    localPort,
                    "localhost",
                     remotePort
            );
            System.out.println("Setting up port forwarding: localhost:" + assignedPort + " -> " +  host + ":" +  remotePort);

            return session;
        } catch (final JSchException e) {
            throw new RuntimeException("Failed to configure SSH tunnel", e);
        }
    }


    public void go() throws Exception {

        StringBuilder outputBuffer = new StringBuilder();

        String Gateway1 = "78.236.233.71"; //"80.185.129.189"; // First level target
        String user1 = "Lewis"; //"zorzan";
        String password = "List2019!"; //"7bY=8>?M<{xC2pa/";
        int port1 = 42222;
        JSch jsch = new JSch();
        JSch.setLogger(new SSHLogger());

        Session session = jsch.getSession(user1, Gateway1, port1);
        session.setPassword(password);
        localUserInfo lui = new localUserInfo();
        session.setUserInfo(lui);
         session.setConfig("StrictHostKeyChecking", "no");

         session.connect();
        String channel_type = "direct-tcpip";
        channel_type = "session";
        session.openChannel(channel_type);


       String Gateway2 = "10.60.1.4"; // The host of the second target
    //    String Gateway2 = "login.hpc.private.list.lu"; // The host of the second target

        // create port from 2233 on local system to port 22 on tunnelRemoteHost
  //      session.setPortForwardingL(2233, Gateway2, 22);

        String user2 = "steven.lewis";
     //   String user2 = "lewis";
        String secondPassword = ClusterProperties.PRIVATE_KEY1_FILE1;
        // create a session connected to port 2233 on the local host.
        Session secondSession = jsch.getSession(user2, Gateway2, 22 );
        jsch.addIdentity(secondPassword);
        localUserInfo lui2 = new localUserInfo();
        secondSession.setUserInfo(lui2);
        secondSession.setConfig("StrictHostKeyChecking", "no");
        session.setProxy(new ProxySSH(session));
        secondSession.connect(); // now we're connected to the secondary system
        secondSession.openChannel(channel_type);


        Channel channel = secondSession.openChannel("exec");
        ((ChannelExec) channel).setCommand("show system information | match \"System Name\"");

        channel.setInputStream(null);

        InputStream stdout = channel.getInputStream();

        channel.connect();
        if (channel.isConnected()) {
            System.out.println("connected");
        }

        while (true) {
            byte[] tmpArray = new byte[1024];
            while (stdout.available() > 0) {
                int i = stdout.read(tmpArray, 0, 1024);
                if (i < 0) break;
                outputBuffer.append(new String(tmpArray, 0, i));
            }
            if (channel.isClosed()) {
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
        }
        stdout.close();

        channel.disconnect();

        secondSession.disconnect();
        session.disconnect();

        System.out.print(outputBuffer.toString());
    }

    class localUserInfo implements UserInfo {
        String passwd;

        public String getPassword() {
            return passwd;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
        }
    }

    public static class ProxySSH implements Proxy {
        public ProxySSH(Session gateway) {
            this.gateway = gateway;
        }
        private Session gateway;
        private ChannelDirectTCPIP channel;
        private InputStream iStream;
        private OutputStream oStream;
        public void connect(SocketFactory socket_factory, String host, int port, int timeout) throws Exception {
            try {
                channel = (ChannelDirectTCPIP)gateway.openChannel("direct-tcpip");
                channel.setHost(host);
                channel.setPort(port);
                iStream = ((ChannelDirectTCPIP)channel).getInputStream();
                oStream = ((ChannelDirectTCPIP)channel).getOutputStream();
                channel.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public InputStream getInputStream() { return iStream; }
        public OutputStream getOutputStream() { return oStream; }
        public Socket getSocket() { return null; }
        public void close() {
            try {
                if(iStream!=null)iStream.close();
                if(oStream!=null)oStream.close();
            } catch(Exception  e) {
                e.printStackTrace();
            }
            iStream = null;
            oStream = null;
            channel.disconnect();
        }
    }
    public static class SSHLogger implements com.jcraft.jsch.Logger {
        public boolean isEnabled(int level) {
            return true;
        }
        public void log(int level, String message) {
            switch(level) {
                case DEBUG: System.out.println("DEBUG:" + message); break;
                case INFO: System.out.println("INFO:" + message); break;
                case WARN: System.out.println("WARNING:" + message); break;
                case ERROR: System.out.println("ERROR:" + message); break;
                case FATAL: System.out.println("FATAL:" + message); break;
            }
        }
    }
}