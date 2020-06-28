package com.lordjoe.blast;

import com.lordjoe.ssh.IJobRunner;
import com.lordjoe.ssh.SlurmClusterRunner;
import com.lordjoe.ssh.JobRunnerUtilities;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * com.lordjoe.blast.BLASTRunnerServlet
 * User: Steve
 * Date: 1/17/20
 */
public class BLASTRunnerServlet extends HttpServlet {

	private com.lordjoe.ssh.SlurmClusterRunner forceClassLoad;
    // public final LocBlast jobs;

    // public BLASTRunnerServlet(LocBlast j) {
    //      jobs = j;
    // }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        String id = request.getParameter("JobId");
        IJobRunner runner = null;
        if (id != null) {
            runner = IJobRunner.fromID(id);

        } else {
            runner = handleBlastLaunch(request);
            Thread trh = new  Thread(runner);
            trh.start();
        }
        ServletContext sc = request.getServletContext();
        RequestDispatcher rd = sc.getRequestDispatcher("/locBlast.monitorBlast?JobId=" + runner.getId());
        rd.forward(request, response);
    }

    public static Map<String, String> getRequestParameters(HttpServletRequest request) {
        Map<String, String> ret = new HashMap<>();
        Map<String, String[]> map = request.getParameterMap();
        for (String s : map.keySet()) {
            String[] strings = map.get(s);
            if (strings.length == 1) {
                ret.put(s, strings[0]);
            }
        }

        return ret;
    }

    protected IJobRunner handleBlastLaunch(HttpServletRequest request) {
        Map<String, String> map = getRequestParameters(request);
        File file = new File("blastParameters.txt");
        String path = file.getAbsolutePath();
        saveParameters(map, file);

        return JobRunnerUtilities.createRunner(map);
    }

    public static void saveParameters(Map<String, String> map, File file) {
        try {
            List<String> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);
            PrintWriter out = new PrintWriter(new FileWriter(file));
            for (String key : keys) {
                out.println(key + "," + map.get(key).replace("\n", "").replace("\r", ""));
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    public static Map<String, String> readParameters(File file) {
        try {
            Map<String, String> ret = new HashMap<>();
            LineNumberReader rdr = new LineNumberReader(new FileReader(file));
            String line = rdr.readLine();
            while (line != null) {
                String[] split = line.split(",");
                if (split.length > 1) {
                    ret.put(split[0], split[1]);
                }
                line = rdr.readLine();
            }
            rdr.close();
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


}
