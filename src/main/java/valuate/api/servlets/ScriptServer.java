package valuate.api.servlets;

import framework.settings.ValuateSettings;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import valuate.api.site.Site;
import valuate.api.site.SiteServer;

public class ScriptServer extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ScriptServer.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {

            String origin = request.getHeader("Origin"); // Try Origin
            if (origin == null) {
                origin = request.getHeader("Referer");  // Fallback solution
            }

            Site site = SiteServer.getSite(Long.parseLong(request.getParameter("for")));

            if (site != null) {
                String cors = null;
                for (String prefix : site.getPrefixes()) {
                    if (origin.startsWith(prefix)) {
                        cors = prefix;  // TODO: extract origin
                        break;
                    }
                }

                response.setContentType("text/javascript;charset=UTF-8");
                response.setHeader("Access-Control-Allow-Origin", cors);

                try (PrintWriter out = response.getWriter()) {
                    out.println("const valuateSiteId = \"" + request.getParameter("for") + "\";");
                    out.println(ValuateSettings.getInstance().getClientScript());
                }
            } else {
                response.sendError(404);
            }
        } catch (IOException | NumberFormatException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

    }
}
