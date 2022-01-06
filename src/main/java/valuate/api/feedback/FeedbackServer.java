package valuate.api.feedback;

import framework.database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import valuate.api.site.SiteServer;
import valuate.api.site.question.QuestionServer;

public class FeedbackServer {

    private static final Logger LOG = Logger.getLogger(FeedbackServer.class.getName());

    public static void registerFeedback(Feedback feedback) {
        long siteId = feedback.getOriginId();
        String questionId = feedback.getQuestionId();

        boolean ok = false;

        if (QuestionServer.questionExists(siteId, questionId)) {    // Question is already known in the database, go ahead with the feedback registration.
            ok = true;
        } else if (!SiteServer.getSite(siteId).isSpamProtect()) {   // If site is not in spam-protect mode, we can register new question automatically.
            QuestionServer.addQuestionByFeedback(feedback);
            ok = true;
        }

        if (ok) {
            ConnectionPool pool = ConnectionPool.getInstance();
            Connection connection = null;
            try {
                connection = pool.getConnection();
                connection.setAutoCommit(false); // Transaction - Start

                PreparedStatement stmt = connection.prepareStatement("INSERT INTO valuate_feedback(site_id, question_id, valuator_ip, valuator_user_agent) "
                        + "VALUES(?, ?, ?, ?) RETURNING feedback_id", Statement.RETURN_GENERATED_KEYS);
                stmt.setLong(1, siteId);
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
}
