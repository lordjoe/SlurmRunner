package com.lordjoe.blast;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * com.lordjoe.blast.JettyTestTabsServlet
 * User: Steve
 * Date: 1/17/20
 */
public class JettyTestTabsServlet  extends HttpServlet  {
    public static final JettyTestTabsServlet[] EMPTY_ARRAY = {};

    public final Jettytest me = new Jettytest();

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response) throws IOException
    {
         me.addTab();
         doGet(request,response);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException
    {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        response.getWriter().println(generatePageText(  request));
    }

    private String generatePageText(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html lang=\"en-US\">");
        sb.append("\n");
        sb.append("<head>");
        sb.append("\n");

        sb.append("<script>");
        sb.append("\n");

        sb.append(OPEN_CITY);
        sb.append("\n");

        if(me.tabs.size() < 2)
        sb.append(  "window.onload=function(){\n" +
                "  document.getElementById(\"Create Tab\").style.display = \"block\";\n"  +
                "  document.getElementById(cityName).style.display = \"block\";\n" +
                "};"
        );

        sb.append("</script>");
        sb.append("\n");

        sb.append(STYLE_CSS);
        sb.append("\n");


        sb.append("</head>");
        sb.append("\n");

        sb.append("<body>");
        sb.append("\n");

        sb.append("<div class = \"tab\">\n");
        sb.append("\n");
        for (int i = 0; i < me.tabs.size(); i++) {
            String text = me.getTabName(i);
            sb.append("<button class=\"tablinks\" onclick=\"openCity(event, \'" + text + "\')\">" + text + "</button>");
            sb.append("\n");
        }
        sb.append("</div>\n");
        sb.append("\n");

        sb.append("<div>\n");
        sb.append("\n");
        for (int i = 0; i < me.tabs.size(); i++) {
            String id = me.getTabName(i);
            String text = me.getTabText(i);
            sb.append("<!-- Tab content -->\n" +
                    "<div id=\"" + id + "\" class=\"tabcontent\">\n" +
                    text +
                    "</div>\n");

        }

        sb.append("</div>\n");
        sb.append("\n");


        sb.append("</body>");

        sb.append("</html>");
        sb.append("\n");
        return sb.toString();
    }



    public static final String OPEN_CITY =
            "function openCity(evt, cityName) {\n" +
                    "  // Declare all variables\n" +
                    "  var i, tabcontent, tablinks;\n" +
                    "\n" +
                    "  // Get all elements with class=\"tabcontent\" and hide them\n" +
                    "  tabcontent = document.getElementsByClassName(\"tabcontent\");\n" +
                    "  for (i = 0; i < tabcontent.length; i++) {\n" +
                    "    tabcontent[i].style.display = \"none\";\n" +
                    "  }\n" +
                    "\n" +
                    "  // Get all elements with class=\"tablinks\" and remove the class \"active\"\n" +
                    "  tablinks = document.getElementsByClassName(\"tablinks\");\n" +
                    "  for (i = 0; i < tablinks.length; i++) {\n" +
                    "    tablinks[i].className = tablinks[i].className.replace(\" active\", \"\");\n" +
                    "  }\n" +
                    "\n" +
                    "  // Show the current tab, and add an \"active\" class to the button that opened the tab\n" +
                    "  document.getElementById(cityName).style.display = \"block\";\n" +
                    "  evt.currentTarget.className += \" active\";\n" +
                    "}\n";

    public static final String STYLE_CSS =

            "<style type=\"text/css\">" +
                    "/* Style the tab */\n" +
                    ".tab {\n" +
                    "  overflow: hidden;\n" +
                    "  border: 1px solid #ccc;\n" +
                    "  background-color: #f1f1f1;\n" +
                    "}\n" +
                    "\n" +
                    "/* Style the buttons that are used to open the tab content */\n" +
                    ".tab button {\n" +
                    "  background-color: inherit;\n" +
                    "  float: left;\n" +
                    "  border: none;\n" +
                    "  outline: none;\n" +
                    "  cursor: pointer;\n" +
                    "  padding: 14px 16px;\n" +
                    "  transition: 0.3s;\n" +
                    "}\n" +
                    "\n" +
                    "/* Change background color of buttons on hover */\n" +
                    ".tab button:hover {\n" +
                    "  background-color: #ddd;\n" +
                    "}\n" +
                    "\n" +
                    "/* Create an active/current tablink class */\n" +
                    ".tab button.active {\n" +
                    "  background-color: #ccc;\n" +
                    "}\n" +
                    "\n" +
                    "/* Style the tab content */\n" +
                    ".tabcontent {\n" +
                    "  display: none;\n" +
                    "  padding: 6px 12px;\n" +
                    "  border: 1px solid #ccc;\n" +
                    "  border-top: none;\n" +
                    "}\n" +
                    "</style>\n";
}
