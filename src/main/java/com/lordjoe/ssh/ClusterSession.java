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
import java.util.Collection;
import java.util.Properties;

/**
 * com.lordjoe.ssh.ClusterSession
 * User: Steve
 * Date: 10/22/2019
 */
public class ClusterSession {
    static java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(ClusterSession.class.getName());

    private static boolean inUse;
    private static ClusterSession gSession;

    public synchronized static ClusterSession getClusterSession() {
        if(gSession == null)   {
            gSession = new ClusterSession();

        }
        if(inUse)
            throw new UnsupportedOperationException("Session In Use");
        inUse = true;
        return gSession;

    }
    public synchronized static void releaseClusterSession(ClusterSession  me) {
        inUse = false;
    }

    private JSch my_jsch;
    private Session my_session;
    private Session final_session;
    private Ssh my_ssh;
    private ChannelSftp cftp;
    private Shell my_shell;

    private ClusterSession() {
     //   SlurmClusterRunner.logMessage("Constructing Cluster Session");
        System.out.println("Constructing Cluster Session");
    }

    /**
     * just make SJ4j shut up and go away
     */
    public static void fixLogging() {
        BasicConfigurator.configure();
        Logger root = Logger.getRootLogger();
        root.setLevel(Level.ERROR);
    }

    public boolean guaranteeDirectory(String directoryName) throws IOException {
        String current = executeOneLineCommand("pwd");
        current = current.trim();
        if (current.endsWith("/"))
            current = current.substring(0, current.length() - 1);
        if (current.equals(directoryName))
            return true;
        if (directoryName.length() > current.length()) {
            if (directoryName.startsWith(current)) {
                return guaranteeDirectory(directoryName);
            }
        } else {
            executeCommand("cd ..");
            return guaranteeDirectory(directoryName);

        }
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public boolean prepareUpload(
            ChannelSftp sftpChannel,
            String path,
            boolean overwrite)
            throws SftpException, IOException, FileNotFoundException {

        boolean result = false;

        // Build romote path subfolders inclusive:
        String[] folders = path.split("/");
        for (String folder : folders) {
            if (folder.length() > 0 && !folder.contains(".")) {
                // This is a valid folder:
                try {
                    sftpChannel.cd(folder);
                } catch (SftpException e) {
                    // No such folder yet:
                    sftpChannel.mkdir(folder);
                    sftpChannel.cd(folder);
                }
            }
        }

        if (true)
            return true;
        // Folders ready. Remove such a file if exists:
        if (sftpChannel.ls(path).size() > 0) {
            if (!overwrite) {
                System.out.println(
                        "Error - file " + path + " was not created on server. " +
                                "It already exists and overwriting is forbidden.");
            } else {
                // Delete file:
                sftpChannel.ls(path); // Search file.
                sftpChannel.rm(path); // Remove file.
                result = true;
            }
        } else {
            // No such file:
            result = true;
        }

        return result;
    }
//
//    // Build romote path subfolders inclusive:
//    String[] folders = path.split("/");
//  for(
//    String folder :folders)
//
//    {
//        if (folder.length() > 0 && !folder.contains(".")) {
//            // This is a valid folder:
//            try {
//                sftpChannel.cd(folder);
//            } catch (SftpException e) {
//                // No such folder yet:
//                sftpChannel.mkdir(folder);
//                sftpChannel.cd(folder);
//            }
//        }
//    }

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
            my_jsch.addIdentity(ClusterProperties.PRIVATE_KEY1_FILE1, ClusterProperties.PUBLIC_KEY1_FILE1, null);
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

    /**
     * this will return non-null if we can talk directly to the cluster
     * @return
     */
    private Session buildDirectSession() {
        String privateKey = "/opt/blastserver/HPC.ppk";
        String endpoint = "10.60.1.4";
        String finaluser="zorzan";
         JSch jsch= getJsch();
        localUserInfo lui=new  localUserInfo();
    //    SlurmClusterRunner.logMessage("Trying Direct Connection");
        try {

            //Connect to the HPC via the Gateway using the localhost, as the session and portforwarding are active
            final_session = jsch.getSession(finaluser, endpoint, 22);
            jsch.addIdentity(privateKey);
            final_session.setUserInfo(lui);
            final_session.setConfig("StrictHostKeyChecking", "no");
            final_session.setTimeout(2000);
            final_session.connect();
             return final_session;
        } catch (JSchException e) {
            try {
      //          SlurmClusterRunner.logMessage("Direct Connect failed");
                my_jsch.removeAllIdentity();
            } catch (JSchException jSchException) {
                String message = jSchException.getMessage();
            }
             return null; // failure try proxy

        }

    }

    public Session buildFinalSession()  {

         if(final_session != null && final_session.isConnected())
             return final_session;
        final_session =  buildDirectSession();
        if(final_session != null && final_session.isConnected())
            return final_session;


        try {
    //        SlurmClusterRunner.logMessage("Building Proxy Connection");
            String Gateway1="78.236.233.71"; // First level target
            String user1="Lewis";
            String password="List2019!";
            // String privateKey = "C:\\Users\\Steve\\.ssh\\HPC.ppk";
           String privateKey = "/home/Steve/.ssh/HPC.ppk";
            String endpoint = "10.60.1.4";
            String finaluser="zorzan";
            String command1="ls -ltr";

            JSch jsch= getJsch();
            my_session =jsch.getSession(user1, Gateway1, 42222);
            my_session.setPassword(password);
            localUserInfo lui=new  localUserInfo();
            my_session.setUserInfo(lui);
            my_session.setConfig("StrictHostKeyChecking", "no");
            my_session.setTimeout(5000);
            my_session.connect();
            my_session.openChannel("direct-tcpip");
            //set port frowarding on the gateway session.
            //Anything connecting on 127.0.0.1 will be redirected trough the Gateway toward the HPC, port 22
            int assinged_port = my_session.setPortForwardingL(0, endpoint, 22);


            //Connect to the HPC via the Gateway using the localhost, as the session and portforwarding are active
            final_session = jsch.getSession(finaluser, "127.0.0.1", assinged_port);
            jsch.addIdentity(privateKey);
            final_session.setUserInfo(lui);
            final_session.setConfig("StrictHostKeyChecking", "no");
    //        SlurmClusterRunner.logMessage("Trying Final Connection");
            final_session.setTimeout(5000);
            final_session.connect();
    //        SlurmClusterRunner.logMessage("Final Connection made");


            return final_session;
        } catch (JSchException e) {
    //        SlurmClusterRunner.logMessage("Final Connection failed " + e.getMessage());
            throw new RuntimeException(e);

        }
    }

    class localUserInfo implements UserInfo {
        String passwd;
        public String getPassword(){ return passwd; }
        public boolean promptYesNo(String str){return true;}
        public String getPassphrase(){ return null; }
        public boolean promptPassphrase(String message){return true; }
        public boolean promptPassword(String message){return true;}
        public void showMessage(String message){}
    }

    public Session getSession() {
        if (final_session != null && final_session.isConnected())
            return final_session;
        final_session = buildFinalSession();
        if(true)
            return final_session;

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
//        SlurmClusterRunner.logMessage("Getting SFTP");
        if (cftp != null)
            return cftp;
        try {
            Session session = getSession();
            if(!session.isConnected()) {
                session.connect();
 //               SlurmClusterRunner.logMessage("Session Connected");
            }
            Channel channel = session.openChannel("sftp");
            if(!channel.isConnected()) {
                channel.connect();
 //               SlurmClusterRunner.logMessage("Channel Connected");
            }
            cftp = (ChannelSftp) channel;

            return cftp;
        } catch (JSchException e) {
 //           SlurmClusterRunner.logMessage("JSchException " + e.getMessage());
            throw new RuntimeException(e);

        }
    }

    /**
     * @param filename the command to execute
     * @return stdout returned by that command
     * @throws IOException
     */
    public void ftpFileGet(File localFile,String remoteFile) throws IOException {

        try {
            ChannelSftp c = getSFTP();

            File parentFile = localFile.getParentFile();
            if(parentFile != null) {
                if(!parentFile.exists())   {
                    if(!parentFile.mkdirs()  )
                        throw new UnsupportedOperationException("Cannot create dir " + parentFile.getAbsolutePath());
                }
            }
  //          SlurmClusterRunner.logMessage("Making file " + localFile.getAbsolutePath());

            OutputStream output = new FileOutputStream(localFile);

//            SlurmClusterRunner.logMessage("Fetching remmote file " + remoteFile + " to " + localFile.getAbsolutePath());
            c.get(remoteFile,output);            //    SftpProgressMonitor monitor=new MyProgressMonitor();
            output.close();
          } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public boolean mkdir(String path) {
        try {
            System.out.println("Making Directory " + path);
              ChannelSftp sftp = getSFTP();
            String[] folders = path.split("/");
            for (String folder : folders) {
                if (folder.length() > 0) {
                    try {
                        sftp.cd(folder);
                    } catch (SftpException e) {
                        sftp.mkdir(folder);
                        sftp.chmod(0777,folder);
                        sftp.cd(folder);
                    }
                }
            }
            return true;
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public boolean cd(String path) {
        ChannelSftp sftp = getSFTP();
        try {
            sftp.cd(path);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    /**
     * @param filename the command to execute
     * @return stdout returned by that command
     * @throws IOException
     */
    public void ftpFileCreate(String filename, String text) throws IOException {
        InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        ftpFileCreate(filename, is);
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
        int exec = shell.exec(command,
                new DeadInputStream(),
                output,
                error
        );

        if(exec != 0)  {
            System.err.println(command + " returned " + exec);
        }
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
        Session session = getSession();
        String out = null;
        int exitStatus = 0;
        System.out.println("Executing  " + command);
        try {
            //Open channel to send commands
            Channel channel= session.openChannel("exec");
            //Execute remote command
            ((ChannelExec)channel).setCommand(command);

            //Read remote output
            channel.setInputStream(null);
            InputStream in=channel.getInputStream();
            if(!channel.isConnected()) {
                channel.connect();
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            channel.setOutputStream(output);
            ((ChannelExec) channel).setErrStream(output);
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    System.out.print(new String(tmp, 0, i));

                }
                if(channel.isClosed()){
                      exitStatus = channel.getExitStatus();

                    if(exitStatus != 0) {
                        System.out.println("exit-status: " + exitStatus);

//                        InputStream errStream = ((ChannelExec) channel).getErrStream();
//                        String s = FileUtilities.readInFile(errStream);
//                        System.out.println("error: " + s);
                    }
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }

            //Disconnect
            channel.disconnect();
           /*
            shell.exec(command,
                    new DeadInputStream(),
                    output,
                    error
            );
            */


            out = new String(output.toByteArray()).trim();
            String errout = new String(error.toByteArray());
            if(exitStatus != 0) {
                System.out.println("Output: " + out);
                System.out.println("Error: " + errout);
            }
            return out;

        } catch (JSchException e) {
            throw new RuntimeException(e);

        }



    }


    public static void recursiveFolderDelete(ChannelSftp channelSftp, String path) {

        try {
            // List source directory structure.
            Collection<ChannelSftp.LsEntry> fileAndFolderList = channelSftp.ls(path);

            // Iterate objects in the list to get file/folder names.
            for (ChannelSftp.LsEntry item : fileAndFolderList) {
                if (!item.getAttrs().isDir()) {
                    channelSftp.rm(path + "/" + item.getFilename()); // Remove file.
                } else if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) { // If it is a subdir.
                    try {
                        // removing sub directory.
                        channelSftp.rmdir(path + "/" + item.getFilename());
                    } catch (Exception e) { // If subdir is not empty and error occurs.
                        // Do lsFolderRemove on this subdir to enter it and clear its contents.
                        recursiveFolderDelete(channelSftp, path + "/" + item.getFilename());
                    }
                }
            }
            channelSftp.rmdir(path); // delete the parent directory after empty
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }

    public void sallocAndRun(String command, int nproocessors) {
        try {
 //           SlurmClusterRunner.logMessage("Ready to salloc ");
            executeCommand("salloc -N" + nproocessors + " srun " + command + " & ");
 //           SlurmClusterRunner.logMessage(" salloc running ");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    public static void main(String[] args) {
        fixLogging();
        ClusterSession me = new ClusterSession();
        ChannelSftp sftp = me.getSFTP();
        recursiveFolderDelete(sftp,args[0]);
        if(true )
            return;
        try {
            String command = BuildBlastFile.buildScript();
            String filename = "myScript.sh";
            me.ftpFileCreate(filename, command);
            me.executeCommand("chmod a+x " + filename);

            //         boolean answer = me.guaranteeDirectory("/mnt/beegfs/home/lewis");
            //    me.ftpFilePut(args[0], args[1]);
            //  String out = me.executeCommand("squeue -u lewis");
            String out = me.executeCommand("salloc  -u lewis");
  //          SlurmClusterRunner.logMessage(out);

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }


}
