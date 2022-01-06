package valuate.api.attribute;

import framework.database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttributeServer {

    private static final Logger LOG = Logger.getLogger(AttributeServer.class.getName());

    // MUST cache, because it is going to be slow
    public static AttributeField getAttributeField(String fieldId) {
        AttributeField field = null;

        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM attribute_field WHERE attribute_field_id=?");
            stmt.setString(1, fieldId);
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                field = new AttributeField(result.getString("attribute_field_id"), result.getBoolean("in_snippet_editor"), result.getBoolean("provided_by_feedback"),
                        result.getBoolean("mandatory"), result.getString("default_value"), result.getString("admin_note"));
            }
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }

        return field;
    }

    public static Map<String, Attribute> getSnippetAttributes(Connection connection, long siteId, String questionId) throws SQLException {
        Map<String, Attribute> attributes = new HashMap<>();

        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM question_attribute WHERE site_id=? AND question_id=?");
        stmt.setLong(1, siteId);
        stmt.setString(2, questionId);

        ResultSet result = stmt.executeQuery();
        while (result.next()) {
            attributes.put(result.getString("attribute_field_id"), new Attribute(result.getString("attribute_field_id"), result.getString("attribute_value")));
        }

        stmt = connection.prepareStatement("SELECT * FROM attribute_field WHERE in_snippet_editor");
        result = stmt.executeQuery();

        while (result.next()) {
            if (!attributes.containsKey(result.getString("attribute_field_id"))) {
                attributes.put(result.getString("attribute_field_id"), new Attribute(result.getString("attribute_field_id"), result.getString("default_value") != null ? result.getString("default_value") : ""));
            }
        }

        return attributes;
    }

    // Can be cached and invalidated when rows in attribute_field table are modified.
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

}
