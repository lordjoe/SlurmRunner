package com.lordjoe.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileZipper {
    //zip a file saving the result over the input file, which shall already have a .zip extension
    public static void zipFile(String filePath) {
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
    }
}
