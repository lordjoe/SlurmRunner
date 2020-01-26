package com.lordjoe.fasta;

import com.lordjoe.ssh.ClusterLauncher;
import com.lordjoe.utilities.FileUtilities;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.UUID;

/**
 * com.lordjoe.fasta.FastaToolsTest
 * User: Steve
 * Date: 1/24/20
 */
public class FastaToolsTest {

    @Test
    public void countFastaEntities() {
        InputStream is = FastaToolsTest.class.getResourceAsStream("/com/lordjoe/fasta/EColi100.fas");
        LineNumberReader rdr = new LineNumberReader(new InputStreamReader(is));
        int count = FastaTools.countFastaEntities(rdr);
        Assert.assertEquals(count, 100);
    }


    @Test
    public void splitFiles() {
        String unique = UUID.randomUUID().toString();
        File tempDir = new File(unique);
        Assert.assertTrue(tempDir.mkdirs());
        InputStream is = FastaToolsTest.class.getResourceAsStream("/com/lordjoe/fasta/EColi100.fas");
        String data = FileUtilities.readInFile(is);
        unique = UUID.randomUUID().toString();
        File originalFile = new File(tempDir, unique + ".fas");
        FileUtilities.writeFile(originalFile, data);

        int originalCount = FastaTools.countFastaEntities(originalFile);

        ClusterLauncher me = new ClusterLauncher(originalFile);
        List<File> files = me.buildFileSplit(tempDir);

        int newCount = FastaTools.countFastaEntities(files);


        List<String> originalList = FastaTools.allFastas(originalFile);
        List<String> newList = FastaTools.allFastas(files);
        for (int i = 0; i < originalList.size(); i++) {
            String orig = originalList.get(i);
            Assert.assertTrue(newList.size() >= i);
            String copy = newList.get(i);
            if (!orig.equals(copy)) {
                System.out.println(orig);
            }
            Assert.assertEquals(orig, copy);


        }

        // cleanup
        for (File file : files) {
            file.delete();
        }
        File[] toDelete = tempDir.listFiles();
        if (toDelete != null) {

            for (int i = 0; i < toDelete.length; i++) {
                File file = toDelete[i];
                file.delete();
            }
        }
        tempDir.delete();
      }
}