package valuate.api.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import valuate.Feedback;

public class Endpoint extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(Endpoint.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //request.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        try (InputStream inputStream = request.getInputStream();) {
            JSONParser parser = new JSONParser();
            JSONObject po = (JSONObject) parser.parse(new InputStreamReader(inputStream));

            Feedback feedback = new Feedback(Long.parseLong((String) po.get("siteId")),
                    (String) po.get("questionId"),
                    (String) po.get("fullUrl"),
                    (String) po.get("question"),
                    (String) po.get("lowest"),
                    (String) po.get("highest"),
                    (Long) po.get("grade"),
                    (String) po.get("comment"),
                    (String) po.get("valuatorId"),
                    (String) po.get("reference"));

            // TODO: validate fullUrl against the recently requested site IDs.
            LOG.info("Received feedback " + feedback);
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

    }
}
