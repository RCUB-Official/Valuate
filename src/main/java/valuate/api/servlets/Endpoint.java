package valuate.api.servlets;

import framework.settings.ValuateSettings;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import valuate.api.feedback.AttributeField;
import valuate.api.feedback.Feedback;
import valuate.api.feedback.FeedbackServer;

public class Endpoint extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(Endpoint.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //request.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String valuatorIP = request.getHeader("X-Forwarded-For");    // This will work for reverse-proxies
        String valuatorUserAgent = request.getHeader("User-Agent");

        try (InputStream inputStream = request.getInputStream();) {
            JSONParser parser = new JSONParser();
            JSONObject po = (JSONObject) parser.parse(new InputStreamReader(inputStream));

            Map attributes = new HashMap<String, String>();

            // Parsing attributes specified in the database
            List<AttributeField> feedbackAttributeFields = FeedbackServer.getFeedbackAttributeFields();

            for (AttributeField af : feedbackAttributeFields) {
                try {
                    String attributeValue = (String) po.get(af.getFieldId());
                    if (attributeValue != null) {
                        attributes.put(af.getFieldId(), attributeValue);
                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }

            Feedback feedback = new Feedback(Long.parseLong((String) po.get("siteId")), (String) po.get("questionId"), valuatorIP, valuatorUserAgent, attributes);
            FeedbackServer.registerFeedback(feedback);

        } catch (Exception ex) {    // LOG EVERY TROUBLE!
            LOG.log(Level.SEVERE, null, ex);
        }

    }
}
