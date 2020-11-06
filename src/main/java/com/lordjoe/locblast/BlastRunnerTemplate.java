package com.lordjoe.locblast;

import com.lordjoe.ssh.LaunchDTO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * com.lordjoe.ssh.BlastRunnerTemplate
 * User: Steve
 * Date: 1/25/20
 */
public class BlastRunnerTemplate extends AbstractJobRunner {
    public BlastRunnerTemplate(String id, Map<String, ?> param) {
        super(id, param);
    }

    public static String concat(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            sb.append(arg) ;
            sb.append("") ;
        }
        return sb.toString().trim();
    }

    public static void requireString(String arg,String required,int position,String[] args) {
        if (arg.equals(required))
            return;
        StringBuilder sb = new StringBuilder();
        sb.append("argnuent string ");
        sb.append(concat(args));
        sb.append(" is wrong");
        sb.append(" at position " + position);
        sb.append(" should be " + required);
        sb.append("not " + arg);

        throw new IllegalStateException(sb.toString());
    }

    public static File makeOutFile(File query) {
        File dir = query.getParentFile();
        String name = query.getName();
        String newName = name.substring(0,name.lastIndexOf('.')) + ".xml";
        return new File(dir,newName);
    }

    public static List<File> makeOutputFiles(List<File> inp)  {
        List<File> ret = new ArrayList<>();
        for (File file : inp) {
            ret.add(makeOutFile(file));
          }
        return ret;
    }

    @Override
    public String getClusterMergeResultZipFileName(LaunchDTO job) {
       throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public LaunchDTO getJob() {
        return null;
    }

    @Override
    public Map<String, ? extends Object> filterProperties(Map<String, ?> in) {
        return null;
    }

    @Override
    public void run() {

    }
}
