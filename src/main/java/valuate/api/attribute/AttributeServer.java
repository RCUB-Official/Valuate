package valuate.api.attribute;

import framework.cache.Cache;
import framework.database.ConnectionPool;
import framework.diagnostics.MonitoredComponent;
import framework.settings.ValuateSettings;
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

public class AttributeServer extends MonitoredComponent {

    private static final Logger LOG = Logger.getLogger(AttributeServer.class.getName());

    private static final AttributeServer instance = new AttributeServer();

    private final Cache cache = new Cache(50);  // Hard-coded default size
    private List<AttributeField> feedbackAttributeFields;   // List of attribute fields provided by feedback.
    private List<AttributeField> snippetAttributeFields; // List of attribute fields found in the snippet.

    private AttributeServer() {
        super("Attribute Server", true);
        feedbackAttributeFields = null;
        snippetAttributeFields = null;
    }

    public static AttributeServer getInstance() {
        return instance;
    }

    @Override
    public synchronized void initialize() {
        cache.resize(ValuateSettings.getInstance().getAttributeFieldCacheSize());
        super.initialize();
    }

    @Override
    public synchronized void shutdown() {
        cache.clear();
        feedbackAttributeFields = null;
        snippetAttributeFields = null;
        super.shutdown();
    }

    // MUST cache, because it is going to be slow
    public static AttributeField getAttributeField(String fieldId) {
        AttributeField field = (AttributeField) instance.cache.get(fieldId);

        if (field == null) { // Cache miss!
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
                    instance.cache.put(field); // Caching now.
                }
            } catch (SQLException | InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                pool.returnConnection(connection);
            }
        }

        return field;
    }

    // If you develop a panel for managing AttributeFields, make sure you reload this server once the editing is done.
    public static List<AttributeField> getFeedbackAttributeFields() {
        if (instance.feedbackAttributeFields == null) { // Initializing the list for the first time after the Attribute Server reload.
            instance.feedbackAttributeFields = new LinkedList<>();

            ConnectionPool pool = ConnectionPool.getInstance();
            Connection connection = null;
            try {
                connection = pool.getConnection();
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM attribute_field WHERE provided_by_feedback");
                ResultSet result = stmt.executeQuery();
                while (result.next()) {
                    String fieldId = result.getString("attribute_field_id");
                    AttributeField field = (AttributeField) instance.cache.get(fieldId);
                    if (field == null) {    // CACHE MISS
                        field = new AttributeField(fieldId, result.getBoolean("in_snippet_editor"),
                                result.getBoolean("provided_by_feedback"), result.getBoolean("mandatory"), result.getString("default_value"),
                                result.getString("admin_note"));
                        instance.cache.put(field); // Caching it now.
                    }

                    instance.feedbackAttributeFields.add(field);
                }
            } catch (SQLException | InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                pool.returnConnection(connection);
            }
        }

        return instance.feedbackAttributeFields;
    }

    // Attributes, not fields, for a question!
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
}
