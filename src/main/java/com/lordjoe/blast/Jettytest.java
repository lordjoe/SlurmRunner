package com.lordjoe.blast;

import de.svenjacobs.loremipsum.LoremIpsum;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.*;

/**
 * com.lordjoe.blast.Jettytest
 * User: Steve
 * Date: 1/17/20
 */
public class Jettytest {
    private LoremIpsum loremIpsum = new LoremIpsum();
    public final Map<String, String> tabs = new HashMap<>();

    public Jettytest() {
        addTab();
        addTab();
    }

    public String generateText() {
        return loremIpsum.getWords(50);
    }

    public void addTab() {
        if (tabs.size() == 0) {
            addFirstTab();
            return;
        }
        String id = "Tab " + Integer.toString(tabs.size() + 1);
        String text = generateText();
        tabs.put(id, text);
    }

    private void addFirstTab() {
        String id = "Make Tab";
        String text = generateFirstText();
        tabs.put(id, text);

    }

    private String generateFirstText() {
        StringBuilder sb = new StringBuilder();

        sb.append("<form method=\"post\" action=\"JettyTestTabsServlet\">\n");
        sb.append("<input id=\"New Tab\" type=\"submit\" value=\"New Tab\" >\n");
        sb.append("</form>\n");
        return sb.toString();
    }

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
        handler.addServletWithMapping(JettyTestTabsServlet.class, "/*");

        return server;
    }

    public static void main(String[] args) throws Exception {
        Jettytest me = new Jettytest();
        // Create a basic jetty server object that will listen on port 8080.
        int port = 8090;
        Server server = createServer(port);

//        URL[] urls = ((URLClassLoader) LocBlast.class.getClassLoader()).getURLs();
//        WebAppContext context;
//        if (urls.length == 1 && urls[0].getFile().endsWith(".war")) {
//            context = new WebAppContext(urls[0].getFile(), contextRoot);
//        } else {
//            context = new WebAppContext("src/main/webapp", contextRoot);
//        }

        WebAppContext restHandler = new WebAppContext();

        restHandler.setResourceBase("./");
        restHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
        // Web
//        ResourceHandler webHandler = new ResourceHandler();
//        webHandler.setResourceBase("./");
//        webHandler.setWelcomeFiles(new String[]{"index.html"});
//
//        // Server
//        HandlerCollection handlers = new HandlerCollection();
//        handlers.addHandler(restHandler);
//        handlers.addHandler(webHandler);
//
//        server.setHandler(handlers);

        // Start things up!
        server.start();

        // The use of server.join() the will make the current thread join and
        // wait until the server thread is done executing.
        server.join();

        System.out.println("running");
    }

    public String getTabName(int i) {
        if (i == 0)
            return ("Create Tab");
        else
            return ("Tab " + i);
    }

    public String getTabText(int i) {
        List<String> ids = new ArrayList<>(tabs.keySet());
        Collections.sort(ids);
        String id = ids.get(i);
        String s = tabs.get(id);
        return s;

    }


}
