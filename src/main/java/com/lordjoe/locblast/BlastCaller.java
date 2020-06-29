package com.lordjoe.locblast;

import com.lordjoe.ssh.SlurmClusterRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * com.lordjoe.locblast.BlastCaller
 * User: Steve
 * Date: 4/19/2020
 */
public class BlastCaller  extends HttpServlet{

    private static Map<String, SlurmClusterRunner>  byID = new HashMap<>();


    public void addRunner(SlurmClusterRunner sr) {
        String id = sr.job.id;
        byID.put(id,sr);
    }


    public  Map<String,String> buildParameters(HttpServletRequest request)   {
        Map<String,String> ret = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String s : parameterMap.keySet()) {
            String[] strings = parameterMap.get(s);
            if(strings.length ==1)
                ret.put(s,strings[0]);
        }
        return ret;
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Map<String, String> c = buildParameters(request);
        ServletContext sc = request.getServletContext();
        RequestDispatcher rd = sc.getRequestDispatcher("/JobState.jsp");


    }
}