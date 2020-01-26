package com.lordjoe.ssh;


import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.jcraft.jsch.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cactoos.io.DeadInputStream;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * com.lordjoe.ssh.ClusterSession
 * User: Steve
 * Date: 10/22/2019
 */
public class ClusterSession {
    public static final ClusterSession[] EMPTY_ARRAY = {};


    private JSch my_jsch;
    private Session my_session;
    private Ssh my_ssh;
    private ChannelSftp cftp;
    private Shell my_shell;

    /**
     * just make SJ4j shut up and go away
     */
    public static void fixLogging() {
        BasicConfigurator.configure();
        Logger root = Logger.getRootLogger();
        root.setLevel(Level.ERROR);
    }

    public boolean guaranteeDirectory(String directoryName)  throws IOException   {
         String current = executeOneLineCommand("pwd");
         current = current.trim();
         if(current.endsWith("/"))
             current = current.substring(0,current.length() -  1) ;
         if(current.equals(directoryName))
             return true;
         if(directoryName.length() > current.length())  {
             if(directoryName.startsWith(current)) {
                 return guaranteeDirectory( directoryName);
             }
         }
         else {
             executeCommand("cd ..");
             return guaranteeDirectory( directoryName);

         }
         throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public Shell getShell() {
        if (my_shell != null)
            return my_shell;
        my_shell = new Shell.Verbose(getSsh());
        return my_shell;
     }

    public JSch getJsch() {
        if (my_jsch != null)
            return my_jsch;
        my_jsch = new JSch();

        //         jsch.addIdentity(privateKey);

        //       jsch.setIdentityRepository(new LocalIdentityRepository());
        try {
   //         my_jsch.addIdentity(ClusterProperties.PRIVATE_KEY1_FILE1, ClusterProperties.PASS_PHRASE1);
            my_jsch.addIdentity(ClusterProperties.PRIVATE_KEY1_FILE1, ClusterProperties.PUBLIC_KEY1_FILE1,null);
        } catch (JSchException e) {
            throw new RuntimeException(e);

        }

        return my_jsch;
    }

    public Ssh getSsh() {
        if (my_ssh != null)
            return my_ssh;
        try {
            my_ssh = new Ssh(
                    ClusterProperties.IP,
                    ClusterProperties.PORT,
                    ClusterProperties.USER,
                    ClusterProperties.PRIVATE_KEY1,
                    ClusterProperties.PASS_PHRASE1
            );
//            my_ssh = new Ssh(
//                    ClusterProperties.IP,
//                    ClusterProperties.PORT,
//                    ClusterProperties.USER,
//                    ClusterProperties.PRIVATE_KEY
//                );
          return my_ssh;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);

        }

    }

    public Session getSession() {
        if (my_session != null)
            return my_session;

        try {
            JSch jsch = getJsch();
            my_session = jsch.getSession(ClusterProperties.USER, ClusterProperties.IP, ClusterProperties.PORT);
            Properties config = new Properties();


            config.setProperty("StrictHostKeyChecking", "no");
            my_session.setConfig(config);

            ConfigRepository configRepository = jsch.getConfigRepository();
            // /     EasyRepo identityRepository = new EasyRepo(jsch);
            //      session.setIdentityRepository(identityRepository);
            HostKeyRepository hkr = jsch.getHostKeyRepository();
            HostKey hkx = new HostKey(ClusterProperties.IP, HostKey.SSHRSA, ClusterProperties.PRIVATE_KEY1.getBytes());
            hkx = new HostKey(ClusterProperties.IP, HostKey.SSHRSA, ClusterProperties.PRIVATE_KEY.getBytes());

            hkr.add(hkx, new MyUserInfo());
            return my_session;
        } catch (JSchException e) {
            throw new RuntimeException(e);

        }
    }

    public ChannelSftp getSFTP() {
        if (cftp != null)
            return cftp;
        try {
            Session session = getSession();
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            cftp = (ChannelSftp) channel;

            return cftp;
        } catch (JSchException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * @param filename the command to execute
     * @return stdout returned by that command
     * @throws IOException
     */
    public void ftpFileGet(String filename) throws IOException {

        try {
            ChannelSftp c = getSFTP();
            SftpProgressMonitor monitor = new MyProgressMonitor();
            monitor.init(SftpProgressMonitor.GET, filename, "dest", Integer.MAX_VALUE);
            StringBuilder sb = new StringBuilder();

            File f1 = new File(filename);
            if (!f1.exists())
                throw new FileNotFoundException(filename);

            //    SftpProgressMonitor monitor=new MyProgressMonitor();
            int mode = ChannelSftp.OVERWRITE;
            c.put(new FileInputStream(f1), f1.getName(), ChannelSftp.OVERWRITE);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }


    /**
     * @param filename the command to execute
     * @return stdout returned by that command
     * @throws IOException
     */
    public void ftpFileCreate(String filename, String text) throws IOException {
        InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        ftpFileCreate( filename, is);
    }

    /**
     * @param filename the command to execute
     * @return stdout returned by that command
     * @throws IOException
     */
    public void ftpFileCreate(String filename, InputStream is) throws IOException {

        try {
            ChannelSftp c = getSFTP();
            SftpProgressMonitor monitor = new MyProgressMonitor();
            monitor.init(SftpProgressMonitor.GET, filename, "dest", Integer.MAX_VALUE);
            StringBuilder sb = new StringBuilder();

            //    SftpProgressMonitor monitor=new MyProgressMonitor();
            int mode = ChannelSftp.OVERWRITE;
            c.put(is, filename, mode);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }


    /**
     * @param filename the command to execute
     * @return stdout returned by that command
     * @throws IOException
     */
    public void ftpFilePut(String filename, String dest) throws IOException {

        try {
            ChannelSftp c = getSFTP();
            SftpProgressMonitor monitor = new MyProgressMonitor();
            monitor.init(SftpProgressMonitor.GET, filename, "dest", Integer.MAX_VALUE);
            StringBuilder sb = new StringBuilder();

            File f1 = new File(dest);

            //    SftpProgressMonitor monitor=new MyProgressMonitor();
            int mode = ChannelSftp.OVERWRITE;
            c.get(filename, new FileOutputStream(f1));
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }


    }

    /**
     * @param command the command to execute
     * @return stdout returned by that command
     * @throws IOException
     */
    public String executeCommand(String command) throws IOException {
        Ssh session = getSsh();
            final Shell shell = new Shell.Verbose(session);


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        shell.exec(command,
                new DeadInputStream(),
                output,
                error
        );

        String out = new String(output.toByteArray());
        String errout = new String(error.toByteArray());

        return out;
    }

    /**
     * @param command the command to execute
     * @return stdout returned by that command
     * @throws IOException
     */
    public String executeOneLineCommand(String command) throws IOException {
        Ssh session = getSsh();
        final Shell shell = new Shell.Verbose(session);


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        shell.exec(command,
                new DeadInputStream(),
                output,
                error
        );

        String out = new String(output.toByteArray());
        String errout = new String(error.toByteArray());

        return out;
    }



    public static void main(String[] args) {
        fixLogging();
        ClusterSession  me = new ClusterSession();
        try {
            String command = BuildBlastFile.buildScript();
            String filename = "myScript.sh";
            me.ftpFileCreate(filename, command);
            me.executeCommand("chmod a+x " + filename);
            me.executeCommand("salloc -N1 srun " + filename + "& ");

            //         boolean answer = me.guaranteeDirectory("/mnt/beegfs/home/lewis");
            //    me.ftpFilePut(args[0], args[1]);
            String out = me.executeCommand("squeue -u lewis");
            System.out.println(out);

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }


}
