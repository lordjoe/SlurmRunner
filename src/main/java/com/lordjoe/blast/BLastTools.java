package com.lordjoe.blast;

import com.devdaily.system.SystemCommandExecutor;
import com.lordjoe.utilities.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * com.lordjoe.blast.BLastTools
 * User: Steve
 * Date: 11/14/2019
 */
public class BLastTools {
    public static final BLastTools[] EMPTY_ARRAY = {};

    // public static final String BLASTCALL = "blastp -query First_Zebrafish.faa -db databases/moust.1.protein.faa -outfmt 8 -out output/z_vs_mouse.xml"
    public static final String BLASTCALL = "blastp -query $FILE -db $DB -outfmt 8 -out $OUTPUT";


    // can run basic ls or ps commands
    // can run command pipelines
    // can run sudo command if you know the password is correct
    public static  String executeCommand(String... args) throws IOException, InterruptedException
    {
        // build the system command we want to run
        List<String> commands = new ArrayList<String>();
     // commands.add(OSValidator.shellCommand());
     // commands.add(OSValidator.continueCommand());
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            commands.add(arg);
        }

        // execute the command
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
            int result = commandExecutor.executeCommand();

        // get the stdout and stderr from the command that was run
        StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
        StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

        // print the stdout and stderr
        System.out.println("The numeric result of the command was: " + result);
        System.out.println("STDOUT:");
        System.out.println(stdout);
        System.out.println("STDERR:");
        System.out.println(stderr);
        return stdout.toString();
    }

    public static String runBlastP(String inFile, String database, String outfile) {
        try {
              StringBuilder output = new StringBuilder();
              String programName = "blastp";
            String[] args = {
                    programName,
                    "-query", inFile, "-db", database, "-outfmt", "5", "-out", outfile
            };
            StringBuilder sb = new StringBuilder();
             for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                sb.append(arg);
                sb.append(" ");
          }
            System.out.println(sb.toString());


                return executeCommand(args);
            } catch (IOException e) {
                throw new RuntimeException(e);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);

            }

//            Process process = (Process) Runtime.getRuntime().exec(
//                    programName,args );
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line + "\n");
//            }
//
//            int exitVal = process.waitFor();
//            if (exitVal == 0) {
//                System.out.println("Success!");
//                System.out.println(output);
//            } else {
//                //abnormal...
//            }
//            return output.toString();
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }



    public static void runBlastsOnSplit(String[] args) throws IOException, InterruptedException {
        int index = 0;
        File inDirectory = new File(args[index++]);
        String database = args[index++];
        File outDirectory = new File(args[index++]);
        if(!outDirectory.exists())
            outDirectory.mkdirs();
        File[] files = inDirectory.listFiles();
        if (files == null)
            return;

        List<String> commands = new ArrayList<>() ;
        commands.add("pwd") ;
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
        int result = commandExecutor.executeCommand();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String in = file.getCanonicalPath();
            String outdir = outDirectory.getCanonicalPath();
            String outfile = outdir + "/" + file.getName().replace(".faa", ".xml");
            File temp = new File(outfile);
            outfile = temp.getCanonicalPath();
            runBlastP(in,database,outfile) ;

        }
    }


    public static void deleteDuplicatesInGoogleDrive() {
        deleteDuplicatedInDrive(new File("T:/GoogleDrive")) ;
    }

    private static void deleteDuplicatedInDrive(File file1) {
        if(!file1.isDirectory())
            return;
        File[] files = file1.listFiles();
        if(files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.isFile())  {
                  if(file.getName().contains("("))   {
                      testAsDuplicate(file);
                  }
            }
            else {
                deleteDuplicatedInDrive( file) ;
            }
        }
    }

    public static final String[] DUPLICATE_TEXT = {
            "(1)",
            "(2)",            "(3)",
            "(4)",            "(5)",
            "(6)",            "(7)",
            "(8)",            "(9)",
            "(10)",            "(11)",
            "(12)",            "(13)",
            "(14)",            "(15)",
            "(16)",            "(17)",
            "(18)",            "(19)",

    };

    private static void testAsDuplicate(File file) {
        String name = file.getName();
        for (int i = 0; i < DUPLICATE_TEXT.length; i++) {
            if(name.contains(DUPLICATE_TEXT[i]))  {
                file.delete();
                return;
            }

        }
        System.out.println(name);
    }


    public static final String BLASTP_PROGRAM = "./blastp.exe";

    private static void runBlastPOnCommandLine(String arg) {
        String[] args = new String[0];
        try {
            String[] strings = FileUtilities.readInAllLines(new File(arg));
            args = strings[0].split(" ");

            String[] cmdArray = new String[args.length + 1] ;
            cmdArray[0] = BLASTP_PROGRAM;
            for (int i = 0; i < args.length; i++) {
                 cmdArray[i + 1] = args[i];
               }
            // create a process and execute cmdArray and currect environment
            Process process = Runtime.getRuntime().exec(cmdArray,null);
            process.waitFor();
            String output =  processOutput(  process);
            String error = processError(  process);
            int returnValue = process.exitValue();

        } catch ( Exception e) {
            throw new RuntimeException(e);

        }

        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            System.out.println(s);
        }
    }

    public static String processOutput(Process process)  {
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
    public static String processError(Process process)  {
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void main(String[] args) throws  Exception {
      //    runBlastsOnSplit(args);
   //     deleteDuplicatesInGoogleDrive();
           runBlastPOnCommandLine(args[0]);
     }



}
