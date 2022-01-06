package valuate.api.site.question;

import valuate.api.attribute.Attribute;
import framework.database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import valuate.api.attribute.AttributeServer;

public class QuestionServer {

    private static final Logger LOG = Logger.getLogger(QuestionServer.class.getName());

    static Question addQuestion(long siteId, String questionId, String questionText) {  // Add by user, manually
        Question question = null;

        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO valuate_question(site_id, question_id) "
                    + "VALUES(?, ?)");
            stmt.setLong(1, siteId);
            stmt.setString(2, questionId);
            stmt.executeUpdate();

            stmt = connection.prepareStatement("INSERT INTO question_attribute (site_id, question_id, attribute_field_id, attribute_value) "
                    + "VALUES(?, ?, ?, ?)");
            stmt.setLong(1, siteId);
            stmt.setString(2, questionId);
            stmt.setString(3, "question"); // TODO: configurable
            stmt.setString(4, questionText);
            stmt.executeUpdate();

            connection.commit();

            question = new Question(siteId, questionId, false, "", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()),
                    AttributeServer.getSnippetAttributes(connection, siteId, questionId));
        } catch (SQLException | InterruptedException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFail) {
                    LOG.log(Level.SEVERE, null, rollbackFail);
                }
            }
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
        return question;
    }

    public static List<Question> getQuestions(long siteId) {
        List<Question> list = new LinkedList<>();

        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM valuate_question WHERE site_id=?");
            stmt.setLong(1, siteId);
            ResultSet result = stmt.executeQuery();

            while (result.next()) {
                list.add(new Question(siteId, result.getString("question_id"), result.getBoolean("lock"), result.getString("user_note"), new Date(result.getTimestamp("created").getTime()),
                        new Date(result.getTimestamp("modified").getTime()), AttributeServer.getSnippetAttributes(connection, siteId, result.getString("question_id"))));
            }
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }

        return list;
    }

    public static Question getQuestion(long siteId, String questionId) {
        Question question = null;

        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM valuate_question WHERE site_id=? AND question_id=?");
            stmt.setLong(1, siteId);
            stmt.setString(2, questionId);
            ResultSet result = stmt.executeQuery();

            if (result.next()) {
                question = new Question(siteId, result.getString("question_id"), result.getBoolean("lock"), result.getString("user_note"), new Date(result.getTimestamp("created").getTime()),
                        new Date(result.getTimestamp("modified").getTime()), AttributeServer.getSnippetAttributes(connection, siteId, result.getString("question_id")));
            }

        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }

        return question;
    }

    public static void updateQuestionByFeedback(long siteId, String questionId, Map<String, Attribute> attributes) {
        Question question = getQuestion(siteId, questionId);
        if (question.isUpdateBySnippet()) {
            boolean update = false; // Assume nothing changed.
            for (String key : attributes.keySet()) {    // Iterating through feedback's attributes.
                Attribute current = question.getAttribute(key);
                if (current != null) {  // Check every attribute of the current question.
                    if (current.getValue().equals(attributes.get(key).getValue())) {
                        update = true;
                        LOG.log(Level.INFO, "Attribute {0} of the question ({1}, \"{2}\") differs. Updating now.", new Object[]{key, siteId, questionId});
                        break;
                    }
                }
            }
            if (update) {
                // Lock is false, otherwise this wouldn't happen.
                updateQuestion(siteId, questionId, questionId, false, question.getUserNote(), attributes);
            }
        }
    }

    static void updateQuestion(long siteId, String oldQuestionId, String questionId, boolean lock, String userNote, Map<String, Attribute> attributes) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false);

            PreparedStatement stmt = connection.prepareStatement("UPDATE valuate_question SET question_id=?, lock=?, user_note=?, modified=current_timestamp "
                    + "WHERE site_id=? AND question_id=?");
            stmt.setString(1, questionId);
            stmt.setBoolean(2, lock);
            stmt.setString(3, userNote);
            stmt.setLong(4, siteId);
            stmt.setString(5, oldQuestionId);
            stmt.executeUpdate();

            for (String key : attributes.keySet()) {
                Attribute attribute = attributes.get(key);

                if (!attribute.isEmpty()) {    // Insert or update the value
                    stmt = connection.prepareStatement("INSERT INTO question_attribute(site_id, question_id, attribute_field_id, attribute_value) "
                            + "VALUES(?, ?, ?, ?) ON CONFLICT (site_id, question_id, attribute_field_id) DO UPDATE SET attribute_value=?");  // Upsert
                    stmt.setLong(1, siteId);
                    stmt.setString(2, questionId);
                    stmt.setString(3, attribute.getFieldId());
                    stmt.setString(4, attribute.getValue());
                    stmt.setString(5, attribute.getValue());
                    stmt.executeUpdate();
                } else {    // Delete attribute if the value is empty
                    stmt = connection.prepareStatement("DELETE FROM question_attribute "
                            + "WHERE site_id=? AND question_id=? AND attribute_field_id=?");
                    stmt.setLong(1, siteId);
                    stmt.setString(2, questionId);
                    stmt.setString(3, attribute.getFieldId());
                    stmt.executeUpdate();
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException | InterruptedException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException failedRollback) {
                    LOG.log(Level.SEVERE, null, failedRollback);
                }
            }
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
    }

    static void setQuestionLock(long siteId, String questionId, boolean lock) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();

            PreparedStatement stmt = connection.prepareStatement("UPDATE valuate_question SET lock=? WHERE site_id=? AND question_id=?");
            stmt.setBoolean(1, lock);
            stmt.setLong(2, siteId);
            stmt.setString(3, questionId);

            stmt.executeUpdate();
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
    }

    static void deleteQuestion(long siteId, String questionId) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();

            PreparedStatement stmt = connection.prepareStatement("DELETE FROM valuate_question WHERE site_id=? AND question_id=?");
            stmt.setLong(1, siteId);
            stmt.setString(2, questionId);

            stmt.executeUpdate();
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
    }
}
