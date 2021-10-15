package valuate.servlets;

import framework.settings.ValuateSettings;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ScriptServer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/javascript;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", request.getParameter("for"));

        try (PrintWriter out = response.getWriter()) {
            out.println(ValuateSettings.getInstance().getClientScript());
        }
    }
}
