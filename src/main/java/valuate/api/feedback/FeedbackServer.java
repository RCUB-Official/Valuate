package valuate.api.feedback;

import framework.database.ConnectionPool;
import framework.settings.ValuateSettings;
import framework.utilities.HashCalculator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import valuate.api.site.SiteServer;

public class FeedbackServer {

    private static final Logger LOG = Logger.getLogger(FeedbackServer.class.getName());

    public static List<AttributeField> getFeedbackAttributeFields() {
        List<AttributeField> list = new LinkedList<>();

        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM attribute_field WHERE provided_by_feedback");
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                list.add(new AttributeField(result.getString("attribute_field_id"), result.getBoolean("in_snippet_editor"), result.getBoolean("provided_by_feedback"),
                        result.getBoolean("mandatory"), result.getString("default_value"), result.getString("admin_note")));
            }
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }

        return list;
    }

    private static boolean checkQuestion(Connection connection, long siteId, String questionId) throws SQLException {
        boolean go = false;

        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM valuate_question WHERE site_id=? AND question_id=?");
        stmt.setLong(1, siteId);
        stmt.setString(2, questionId);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {    // Question is already known in the database, go ahead with the insertion.
            go = true;
        } else if (!SiteServer.getSite(siteId).isSpamProtect()) {   // If site is not in spam-protect mode, we can register new question automatically.
            stmt = connection.prepareStatement("INSERT INTO valuate_question(site_id, question_id, lock, user_note) "
                    + "VALUES(?, ?, false, 'Added automatically upon receiving a feedback.')");
            stmt.setLong(1, siteId);
            stmt.setString(2, questionId);
            stmt.executeUpdate();
            go = true;
            LOG.log(Level.INFO, "Added a question for site {0} with question_id \"{1}\".", new Object[]{siteId, questionId});
        }

        return go;
    }

    public static void registerFeedback(Feedback feedback) {
        ValuateSettings settings = ValuateSettings.getInstance();

        String questionId = feedback.getQuestionId();
        if (questionId.startsWith(settings.getQuestionIdAutoPrefix())) {
            /* Question has an auto-generated id, but only for browser-level document use.
            Now we are going to generate that id as question_id = md5(question), to use it as a composite primary key (site_id, question_id). */
            questionId = HashCalculator.md5(feedback.getAttributes().get(settings.getQuestionAttributeFieldId()));
        }

        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false); // Transaction - Start

            if (checkQuestion(connection, feedback.getOriginId(), questionId)) {
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
                        stmt.setString(3, feedback.getAttributes().get(af));
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
