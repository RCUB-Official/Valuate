package valuate.api.servlets;

import framework.settings.ValuateSettings;
import framework.utilities.HashCalculator;
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
import valuate.api.attribute.Attribute;
import valuate.api.attribute.AttributeField;
import valuate.api.attribute.AttributeServer;
import valuate.api.feedback.Feedback;
import valuate.api.feedback.FeedbackServer;
import valuate.api.site.SiteServer;
import valuate.api.site.question.QuestionServer;

public class Endpoint extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(Endpoint.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ValuateSettings settings = ValuateSettings.getInstance();

        //request.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String valuatorIP = request.getHeader("X-Forwarded-For");    // This will work for reverse-proxies
        String valuatorUserAgent = request.getHeader("User-Agent");

        try (InputStream inputStream = request.getInputStream();) {
            JSONParser parser = new JSONParser();
            JSONObject po = (JSONObject) parser.parse(new InputStreamReader(inputStream));

            long siteId = Long.parseLong((String) po.get("siteId"));
            String questionId = (String) po.get("questionId");

            Map<String, Attribute> attributes = new HashMap<>();

            // Parsing attributes specified in the database
            List<AttributeField> feedbackAttributeFields = AttributeServer.getFeedbackAttributeFields();

            for (AttributeField af : feedbackAttributeFields) {
                try {
                    String attributeValue = (String) po.get(af.getFieldId());
                    if (attributeValue != null) {
                        attributes.put(af.getFieldId(), new Attribute(af.getFieldId(), attributeValue));
                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }

            if (questionId.startsWith(settings.getQuestionIdAutoPrefix())) {
                /* Question has an auto-generated id, but only for browser-level document use.
            Now we are going to generate that id as question_id = md5(question), to use it as a composite primary key (site_id, question_id). */
                questionId = HashCalculator.md5(attributes.get(settings.getQuestionAttributeFieldId()).getValue());
            }

            Feedback feedback = new Feedback(siteId, questionId, valuatorIP, valuatorUserAgent, attributes);

            // Checking if the site exits, quietly ignore if not (endpoint is the place for most validations).
            if (SiteServer.getSite(siteId) != null) {
                // Register the feedback, if it passes validations
                FeedbackServer.registerFeedback(feedback);

                // Update question if the question is not locked
                QuestionServer.updateQuestionByFeedback(feedback);
            }
        } catch (Exception ex) {    // LOG EVERY TROUBLE!
            LOG.log(Level.SEVERE, null, ex);
        }

    }
}
