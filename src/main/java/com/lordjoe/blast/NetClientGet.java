package com.lordjoe.blast;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetClientGet {

    public static int SERVER_PORT = 8007;

    public static boolean guaranteeServer() {
        try {
            String url = "http://localhost:" + SERVER_PORT +   "/ping" ;
            String response =  getResponse(url);
            while(response == null || !response.contains("{ \"status\": \"ok\"}") ) {
                Thread.sleep(2000);
                launchServer();
                response =  getResponse(url);
            }
            return true;
        } catch ( Exception e) {

            return false;
        }
    }

    public static String launchServer() {
        String usreDir = System.getProperty("user.dir");
        String command = "java -jar /opt/blastserver/SLURM_Runner.jar com.lordjoe.server.JettyServer";
        String response = captureExecOutput(command);
        return response;
    }


    // see https://stackoverflow.com/questions/42045499/send-a-simple-json-object-in-java-to-a-servlet
    public static boolean callClientWithJSon(String server,JSONObject json) {
        URL url;
        HttpURLConnection connection = null;
        ObjectOutputStream out;
        try {
            url = new URL("http://localhost:" + SERVER_PORT + "/" + server);     //Creating the URL.
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            //Send request
            OutputStream os = connection.getOutputStream();
            if(json != null) {
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                //	        System.out.println(json.toString());
                String s = json.toString();
                osw.write(s);
                osw.flush();
                osw.close();
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param command non-null command to execute
     * @return non-null output string
     * @throws RuntimeException on error
     */
    public static String captureExecOutput(String command) throws RuntimeException {
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            String buf;
            BufferedReader se = new BufferedReader
                    (new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String aline = buf = se.readLine();
            while (aline != null) {
                if("{ \"status\": \"ok\"}".equals(aline))
                    break;
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(buf);
                aline = buf = se.readLine();
            }
            se.close();

            return sb.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
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
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                answer.append(output);
                answer.append("\n");
            }

            conn.disconnect();
            return answer.toString();

        } catch (MalformedURLException e) {

            return null;

        } catch (IOException e) {

            return null;

        }

    }

}
