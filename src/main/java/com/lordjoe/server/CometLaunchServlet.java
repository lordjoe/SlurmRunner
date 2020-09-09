package com.lordjoe.server;

import com.lordjoe.ssh.IJobRunner;
import com.lordjoe.ssh.JobRunnerUtilities;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * com.lordjoe.server.PingServlet
 * User: Steve
 * Date: 6/25/20
 */
public class CometLaunchServlet extends HttpServlet {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        doPost( request,
                 response);
    }

    /**
     * see  https://stackoverflow.com/questions/42045499/send-a-simple-json-object-in-java-to-a-servlet
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            StringBuilder sb = new StringBuilder();
            BufferedReader br = request.getReader();
            String str = null;
            while ((str = br.readLine()) != null) {
                sb.append(str);
              }
               JSONObject json = new JSONObject(sb.toString());
        //    String name = jObj.getString("Name");
         //   String pwd = jObj.getString("Pwd");
        //    String command = jObj.getString("Command");

            response.setContentType("application/json");
            response.setHeader("Cache-Control", "nocache");
            response.setCharacterEncoding("utf-8");
            String s = json.toString().replace(",",",\n");
       //     System.out.println(s);
            Map<String,Object> map = json.toMap();

            IJobRunner runner = JobRunnerUtilities.createRunner(map);


            executorService.submit(runner);
            
            response.setStatus(200);
            response.getWriter().println("{\"job_status\":\"started\"}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
