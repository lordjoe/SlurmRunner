package com.lordjoe.server;

import com.lordjoe.blast.JSonRunner;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * com.lordjoe.server.JettyServer
 * User: Steve
 * Date: 6/25/20
 */
public class JettyServer implements Runnable {

    public static final int SERVER_PORT = 8007;

    private static Server server;
    private static int maxThreads = 100;
    private static int minThreads = 10;
    private static int idleTimeout = 120;

    private static QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

    public static void stop() {
         try {
             getResponse("http://localhost:8007/stop");
         }
         catch(Exception ex)  {
             // we expect this to fail
         }
    }

    public static Server start() throws Exception {
        server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(SERVER_PORT);

        // The ServletHandler is a dead simple way to create a context handler
        // that is backed by an instance of a Servlet.
        // This handler then needs to be registered with the Server object.
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        

        handler.addServletWithMapping(PingServlet.class, "/ping");
        handler.addServletWithMapping(StopServlet.class, "/stop");
        handler.addServletWithMapping(BlastLaunchServlet.class, "/runProgram");
        handler.addServletWithMapping(CometLaunchServlet.class, "/runComet");
        server.setConnectors(new Connector[]{connector});
        return server;
    }

    public static class  StopServlet  extends HttpServlet {
        protected void doGet(
                HttpServletRequest request,
                HttpServletResponse response)
                throws ServletException, IOException {
             try {
                 server.stop();
             }
             catch(Exception e)   {
                 return;
             }
        }

    }

    // http://localhost:8080/RESTfulExample/json/product/get
    public static String getResponse(String urlStr) {

        StringBuffer answer = new StringBuffer();
        try {

            URL url = new URL(urlStr);
//			URL url = new URL("http://localhost:8080/RESTfulExample/json/product/get");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
             while ((output = br.readLine()) != null) {
                answer.append(output);
                answer.append("\n");
            }

            conn.disconnect();
            System.out.println(answer);
            return answer.toString();

        } catch (MalformedURLException e) {

            return null;

        } catch (IOException e) {

            return null;

        }

    }

    @Override
    public void run() {
        try {
            Server s = start();
            s.start();
            System.out.println("launched");
            String pingResponse = getResponse("http://localhost:8007/ping");
            System.out.println(pingResponse);
            s.join();
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

    private static void handleAddedArguments(String[] args) {
        String idx = UUID.randomUUID().toString();

        Map<String, String> map = new HashMap<>();
        map.put("JobId",idx);
        map.put("program","blastp");
        //  -user slewis ./blastp -db swissprot -query EColi100.fas -db swissprot -remote -outfmt 16 -out EColi100.xml -email lordjoe2000@gmail.com
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.startsWith("-")) {
                String key = arg.substring(1);
                i++;
                String value = args[i];
                map.put(key,value);
            }
        }
        JSonRunner jr = new JSonRunner(map);

        jr.startJob();

    }
    public static void main(final String[] args) throws Exception {
        Thread t = Thread.currentThread();
        stop();
        Thread thr =  new Thread(new JettyServer());
        thr.start();
        t.sleep(1000);

        if(args.length > 1)  {
            handleAddedArguments(args);
        }
        thr.join();
    }


}
