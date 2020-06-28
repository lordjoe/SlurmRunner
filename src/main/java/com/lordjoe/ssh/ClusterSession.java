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
            System.out.println("Session Connected");
            Channel channel = session.openChannel("sftp");
            channel.connect();
            cftp = (ChannelSftp) channel;
            System.out.println("Channel Connected");

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
            SlurmClusterRunner.logMessage("Making file " + localFile.getAbsolutePath());

            OutputStream output = new FileOutputStream(localFile);

            SlurmClusterRunner.logMessage("Fetching remmote file " + remoteFile + " to " + localFile.getAbsolutePath());
            c.get(remoteFile,output);            //    SftpProgressMonitor monitor=new MyProgressMonitor();
            output.close();
          } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public boolean mkdir(String path) {
        try {
            ChannelSftp sftp = getSFTP();
            String[] folders = path.split("/");
            for (String folder : folders) {
                if (folder.length() > 0) {
                    try {
                        sftp.cd(folder);
                    } catch (SftpException e) {
                        sftp.mkdir(folder);
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
            SlurmClusterRunner.logMessage("Ready to salloc ");
            executeCommand("salloc -N" + nproocessors + " srun " + command + " & ");
            SlurmClusterRunner.logMessage(" salloc running ");
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
            SlurmClusterRunner.logMessage(out);

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }


}
