package com.lordjoe.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * com.lordjoe.server.PingServlet
 * User: Steve
 * Date: 6/25/20
 */
public class PingServlet  extends HttpServlet {
    static Logger LOG = Logger.getLogger(PingServlet.class.getName());

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        LOG.warning("pinged");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{ \"status\": \"ok\"}");
    }
}
