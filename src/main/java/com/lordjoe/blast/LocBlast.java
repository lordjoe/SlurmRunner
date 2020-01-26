package com.lordjoe.blast;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * com.lordjoe.blast.LocBlast
 * User: Steve
 * Date: 1/16/20
 */
public class LocBlast {
    public final BLASTJobSet jobs = new BLASTJobSet();


    public static Server createServer(int port) {
        // Note that if you set this to port 0 then a randomly available port
        // will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        Server server = new Server(port);

        // The ServletHandler is a dead simple way to create a context handler
        // that is backed by an instance of a Servlet.
        // This handler then needs to be registered with the Server object.
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        // Passing in the class for the Servlet allows jetty to instantiate an
        // instance of that Servlet and mount it on a given context path.

        // IMPORTANT:
        // This is a raw Servlet, not a Servlet that has been configured
        // through a web.xml @WebServlet annotation, or anything similar.
        handler.addServletWithMapping(Hello1Servlet.class, "/*");

        return server;
    }

    public static void main(String[] args) throws Exception {
        LocBlast me = new LocBlast();
        // Create a basic jetty server object that will listen on port 8080.
        int port = 8090;
        Server server = createServer(port);

        URL[] urls = ((URLClassLoader) LocBlast.class.getClassLoader()).getURLs();
//        WebAppContext context;
//        if (urls.length == 1 && urls[0].getFile().endsWith(".war")) {
//            context = new WebAppContext(urls[0].getFile(), contextRoot);
//        } else {
//            context = new WebAppContext("src/main/webapp", contextRoot);
//        }

        WebAppContext restHandler = new WebAppContext();

        //    restHandler.setResourceBase("./data");
        //    restHandler.setClassLoader(Thread.currentThread().getContextClassLoader());

        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);

        servletHandler.addServletWithMapping(Hello1Servlet.class, "/");

        // Web
        ResourceHandler webHandler = new ResourceHandler();
        webHandler.setResourceBase("./");
        webHandler.setWelcomeFiles(new String[]{"index.html"});

        // Server
        HandlerCollection handlers = new HandlerCollection();
        //     handlers.addHandler(restHandler);
        handlers.addHandler(servletHandler);

        server.setHandler(handlers);

        // Start things up!
        server.start();

        // The use of server.join() the will make the current thread join and
        // wait until the server thread is done executing.
        server.join();


    }

    public static final String getLobBlastPage()
    {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream str = LocBlast.class.getResourceAsStream("/com/lordjoe/blast/locBLAST.html");
            LineNumberReader rdr = new LineNumberReader(new InputStreamReader(str));
            String line = rdr.readLine();
            while(line != null) {
                sb.append(line);
                sb.append("\n") ;
                line = rdr.readLine();
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    @SuppressWarnings("serial")
    public static class Hello1Servlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws IOException
        {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(getLobBlastPage());
        }
    }
}
