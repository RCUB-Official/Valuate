package valuate.api.feedback;

import framework.database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import valuate.api.attribute.Attribute;
import valuate.api.attribute.AttributeField;
import valuate.api.attribute.AttributeServer;
import valuate.api.site.SiteServer;

public class FeedbackServer {

    private static final Logger LOG = Logger.getLogger(FeedbackServer.class.getName());

    // Might be appropriate to move insertion by feedback into the question server in the next refactoring, and split into two functions.
    private static boolean checkQuestion(Connection connection, long siteId, String questionId, Map<String, Attribute> attributes) throws SQLException {
        boolean go = false;

        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM valuate_question WHERE site_id=? AND question_id=?");
        stmt.setLong(1, siteId);
        stmt.setString(2, questionId);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {    // Question is already known in the database, go ahead with the feedback registration.
            go = true;
        } else if (!SiteServer.getSite(siteId).isSpamProtect()) {   // If site is not in spam-protect mode, we can register new question automatically.
            stmt = connection.prepareStatement("INSERT INTO valuate_question(site_id, question_id, lock, user_note) "
                    + "VALUES(?, ?, false, 'Added automatically upon receiving a feedback.')");
            stmt.setLong(1, siteId);
            stmt.setString(2, questionId);
            stmt.executeUpdate();

            for (String key : attributes.keySet()) {
                AttributeField field = AttributeServer.getAttributeField(key);
                if (field != null) {    // Must check, because it came by HTTP request
                    if (field.isInSnippetEditor()) {    // Only if it belongs to the snippet_editor (question_attribute table).
                        stmt = connection.prepareStatement("INSERT INTO question_attribute(site_id, question_id, attribute_field_id, attribute_value) "
                                + "VALUES(?, ?, ?, ?)");
                        stmt.setLong(1, siteId);
                        stmt.setString(2, questionId);
                        stmt.setString(3, key);
                        stmt.setString(4, attributes.get(key).getValue());
                        stmt.executeUpdate();
                    }
                }

            }

            go = true;
            LOG.log(Level.INFO, "Added a question for site {0} with question_id \"{1}\".", new Object[]{siteId, questionId});
        }

        return go;
    }

    public static void registerFeedback(Feedback feedback) {
        String questionId = feedback.getQuestionId();

        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false); // Transaction - Start

            if (checkQuestion(connection, feedback.getOriginId(), questionId, feedback.getAttributes())) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO valuate_feedback(site_id, question_id, valuator_ip, valuator_user_agent) "
                        + "VALUES(?, ?, ?, ?) RETURNING feedback_id", Statement.RETURN_GENERATED_KEYS);
                stmt.setLong(1, feedback.getOriginId());
                stmt.setString(2, questionId);
                stmt.setString(3, feedback.getValuatorIP());
                stmt.setString(4, feedback.getValuatorUserAgent());

                stmt.executeUpdate();
                ResultSet key = stmt.getGeneratedKeys();
                if (key.next()) {   // Insertion was successful, now it's time to insert feedback's attributes.
                    Long feedbackId = key.getLong("feedback_id");
                    for (String af : feedback.getAttributes().keySet()) {
                        stmt = connection.prepareStatement("INSERT INTO feedback_attribute(feedback_id, attribute_field_id, attribute_value) "
                                + "VALUES(?, ?, ?)");
                        stmt.setLong(1, feedbackId);
                        stmt.setString(2, af);
                        stmt.setString(3, feedback.getAttributes().get(af).getValue());
                        stmt.executeUpdate();
                    }
                }
            }

            connection.commit();
            connection.setAutoCommit(true); // Transaction - End
        } catch (SQLException | InterruptedException ex) {
            if (connection != null) {
                try {
                    connection.rollback();  // Transaction - Failed, ROLLBACK
                } catch (SQLException rollbackFail) {
                    LOG.log(Level.SEVERE, null, rollbackFail);
                }
            }
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
    }
}
