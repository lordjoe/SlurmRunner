package com.lordjoe.blast;

import com.lordjoe.ssh.IJobRunner;
import com.lordjoe.ssh.JobState;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * com.lordjoe.blast.BLASTRunnerServlet
 * User: Steve
 * Date: 1/17/20
 */
public class BLASTStateServlet extends HttpServlet {

    // public final LocBlast jobs;

    // public BLASTRunnerServlet(LocBlast j) {
    //      jobs = j;
    // }

    public static final String[] colors = {
            "Yellow",
            "LightSkyBlue",
            "Chartreuse",
            "Cyan",
            "Plum",
            "Beige",
            "LightGreen",
            "LightPink",
            "Gold",
            "Teal",
            "PowderBlue",
            "Salmon",
            "SandyBrown"
    };

    private Map<String, String> lastText = new HashMap<>();

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws  IOException {
        String id = request.getParameter("JobId");
        String pprogramName = request.getParameter("program");
        IJobRunner runner = IJobRunner.fromID(id);
         String pageText = "";
        if(runner != null ) {
            JobState lastState = runner.getLastState();
            JobState currentState = runner.getState();
            String currentStateStr = currentState.toString();
             if (lastState != null && lastState.toString().equals(currentStateStr)) {
                pageText = lastText.get(id);
             }
             else {
                 if(currentState == JobState.JobFinished)    {
                     pageText = generateShowResults(id, currentState, runner);

                 }
                 if (pageText.length() == 0) {
                     pageText = generateText(id, currentState, runner);
                     lastText.put(id, pageText);
                 }
                 runner.setLastState(currentState);
             }
        }
        response.setContentType("text/html");
        ServletOutputStream sout = response.getOutputStream();
        sout.print(pageText);

    }

    public static final String MOVE_ON = "<script type=\"text/javascript\">\n" +
            "parent.window.onload = function() {\n" +
            "\n" +
            "   Window.parent.location.replace(\"http://www.w3schools.com\");\n" +
            "   }\n" +
            "</script>/n";

    public static String generateShowResults(String id, JobState currentState, IJobRunner runner) {
        StringBuilder sb = new StringBuilder();
        sb.append(MOVE_ON);
        return sb.toString();
    }


    public static String generateText(String id, JobState currentState, IJobRunner runner) {
        StringBuilder sb = new StringBuilder();
        sb.append("       <meta http-equiv=\"refresh\" content=\"2\">\n");
        sb.append("        <table style=\"width:50%; border:1 \">\n");
        sb.append(        "            <tr>\n");
        sb.append(       "                <th colspan=\"3\" >Action</th>\n");
        sb.append(       "            </tr>\n");
        JobState[] values = JobState.values();
        for (int i = values.length - 1; i > 0 ; i--) {
              JobState js = values[i];
            if(js.position == 0)
                continue;
              if(js.next == null)
                  continue;
              if(js.position > currentState.position)
                  continue;
            sb.append(        "            <tr align=\"right\">\n");
            sb.append(       "                <td colspan=\"3\" bgcolor=" + colors[i] +  ">" + js.toString() + "</td>\n");
            sb.append(       "            </tr>\n");

        }
       sb.append("        </table>");

        return sb.toString();
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        doGet(request, response);
    }


}
