package com.lordjoe.utilities;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileZipper {
    private static final int BUFFER_SIZE = 2048;
    //zip a file saving the result over the input file, which shall already have a .zip extension
/*    public static void zipFileOld(String filePath) {
        try {
            File file = new File(filePath);
            String zipFileName = file.getName().substring(0,file.getName().lastIndexOf(".")).concat(".zip"); // change file extension to .zip

            Path path = Paths.get(filePath);
            String directory = path.getParent().toString();

            String s = file.getPath().replace(file.getName(), "") + zipFileName;
            File outFile = new File(s);
            FileOutputStream fos = new FileOutputStream(outFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            zos.putNextEntry(new ZipEntry(file.getName()));

            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            zos.close();


            FileUtilities.setReadWritePermissions(outFile);
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

        } catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", filePath);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
    }*/
    //Alternative Method
    public static void zipFile(String inFile) throws IOException {
        File file = new File(inFile);
        String zipFileName = file.getName().substring(0,file.getName().lastIndexOf(".")).concat(".zip"); // change file extension to .zip

        Path path = Paths.get(inFile);
        String directory = path.getParent().toString();
        String s = file.getPath().replace(file.getName(), "") + zipFileName;

        BufferedInputStream origin = null;
        File outFile = new File(s);
        FileOutputStream fos = new FileOutputStream(outFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        try {
            byte data[] = new byte[BUFFER_SIZE];

            FileInputStream fi = new FileInputStream(inFile);
            origin = new BufferedInputStream(fi, BUFFER_SIZE);
            try {
                ZipEntry entry = new ZipEntry(inFile.substring(inFile
                        .lastIndexOf("/") + 1));
                zos.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                    zos.write(data, 0, count);
                }
            } finally {
                origin.close();
            }
        }
        catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", inFile);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
        finally {
            zos.close();
            System.out.println("ziping done");
        }

    }

    public static void main(String args[]) {
        try {
            zipFile("C:\\opt\\Outputs\\Comet\\K562-1.pep.xml");
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }
}
