package com.lordjoe.blast;

import com.lordjoe.ssh.IJobRunner;
import com.lordjoe.ssh.JobState;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * com.lordjoe.blast.BLASTRunnerServlet
 * User: Steve
 * Date: 1/17/20
 */
public class BLASTMonitorServlet extends HttpServlet {

    // public final LocBlast jobs;

    // public BLASTRunnerServlet(LocBlast j) {
    //      jobs = j;
    // }


    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws   IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws  IOException {
        String lastState = request.getParameter("state");
        String id = request.getParameter("JobId");
        String pprogramName = request.getParameter("program");
        IJobRunner runner = IJobRunner.fromID(id);
        JobState currentState = runner.getState();
        String currentStateStr = currentState.toString();
        String pageText = null;

        if (pageText == null) {
            pageText = generateText(id, currentStateStr, runner);
        }
        response.setContentType("text/html");
        ServletOutputStream sout = response.getOutputStream();
        sout.print(pageText);

    }

    public static String generateText(String id, String currentStateStr, IJobRunner runner) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en-US\">\n" +
                "<head>\n" +
                "<title>Job Progress</title>\n");
        sb.append("<script>\n" +
                "  function resizeIframe(obj) {\n" +
                "    obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';\n" +
                "  }\n" +
                "</script>\n");
         sb.append("</head>\n");
        sb.append("<body>\n");
        String url = generateStateUrl(id,currentStateStr,runner);
        sb.append("   <iframe src=\"" + url +  "\" frameborder=\"0\" scrolling=\"no\" onload=\"resizeIframe(this)\" >\n" +
                 "    </iframe>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        return sb.toString();
    }

    public static String generateStateUrl(String id, String currentStateStr, IJobRunner runner) {
        StringBuilder sb = new StringBuilder();
        sb.append("./jobstate.blastState?JobId=" + id);
        return sb.toString();
    }



}
