package com.lordjoe.blast;

import com.devdaily.system.SystemCommandExecutor;
import com.lordjoe.utilities.FileUtilities;

import java.io.*;
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

    public static final String FOOTER_N =
            "</BlastOutput_iterations>\n" +
                    "</BlastOutput>\n";
    public static final String FOOTER_P =
                           "</BlastXML2>\n";

    public static void mergeXMLFiles_P(String outFile, File[] inFiles ) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(outFile));
            String header = readHeaderP(inFiles[0]);
            int[] hitnum =  new int[1]  ;
            out.print(header);
            for (int i = 0; i < inFiles.length; i++) {
                File inFile = inFiles[i];
                 addHits_P(out, inFile,hitnum);
            }
            out.println(FOOTER_P);
            out.close();
        } catch (IOException e) {
            usage();
            throw new RuntimeException(e);

        }
    }

    private static void addHits_N(PrintWriter out, File inFile,int[] hitnum) {
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            while(line != null && !line.startsWith("<Iteration>"))
                line = rdr.readLine();
            while (line != null) {
                if(line.startsWith("  <Iteration_iter-num>") ) {
                    hitnum[0]++;
                    line =  "  <Iteration_iter-num>" +   hitnum[0] +   "</Iteration_iter-num>";
                }
                if( line.startsWith("  <Iteration_query-ID>Query_") )  {
                    line =  "  <Iteration_query-ID>Query_" +   hitnum[0] +   "</Iteration_query-ID>";
                }
                if (line.startsWith("</BlastOutput_iterations>")) {
                    rdr.close();
                    return;
                }
                out.println(line);
                line = rdr.readLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    private static void addHits_P(PrintWriter out, File inFile,int[] hitnum) {
        try {
            System.out.println(inFile.getAbsolutePath());
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            while(line != null && !line.startsWith("<BlastOutput2>"))
                line = rdr.readLine();
            while (line != null) {
                  if (line.startsWith("</BlastXML2>")) {
                    rdr.close();
                    return;
                }
                out.println(line);
                line = rdr.readLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    private static String readHeaderN(File inFile) {
        try {
            StringBuilder sb = new StringBuilder();
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            while (line != null) {
                if (line.trim().startsWith("<Iteration>")) {
                    rdr.close();
                    return sb.toString();
                }
                sb.append(line);
                sb.append("\n");
                line = rdr.readLine();
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
    private static String readHeaderP(File inFile) {
        try {
            StringBuilder sb = new StringBuilder();
            LineNumberReader rdr = new LineNumberReader(new FileReader(inFile));
            String line = rdr.readLine();
            while (line != null) {
                if (line.trim().startsWith("<BlastOutput2>")) {
                    rdr.close();
                    return sb.toString();
                }
                sb.append(line);
                sb.append("\n");
                line = rdr.readLine();
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
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


    public static void mergeXMLFiles(String[] args) throws IOException, InterruptedException {
        int index = 0;
        if(args.length < 2)  {
            usage();
            return;
        }
         File inDirectory = new File(args[index++]);
        if(!inDirectory.exists() || !inDirectory.isDirectory()) {
            usage();
            return;
        }
        File outfile = new File(args[index++]);
         File[] files = inDirectory.listFiles();
        if (files == null) {
            usage();
            return;
        }
         mergeXMLFiles_P(outfile.getCanonicalPath(),files );

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

    public static void usage()
    {
        System.out.println("MergeXMLFiles directorty_with_files_toMerge mergedfile");
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
      mergeXMLFiles(args);
   //     runBlastPOnCommandLine(args[0]);
     }



}
