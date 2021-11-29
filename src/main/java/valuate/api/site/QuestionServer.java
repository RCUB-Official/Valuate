package valuate.api.site;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QuestionServer {

    private static Map<String, Attribute> getAttributes(Connection connection, long siteId, String questionId) throws SQLException {
        Map<String, Attribute> attributes = new HashMap<>();

        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM snippet_attribute_value WHERE site_id=? AND question_id=?");
        stmt.setLong(1, siteId);
        stmt.setString(2, questionId);

        ResultSet result = stmt.executeQuery();
        while (result.next()) {
            attributes.put(result.getString("attribute_id"), new Attribute(result.getString("attribute_id"), result.getString("attribute_value")));
        }

        stmt = connection.prepareStatement("SELECT * FROM snippet_attribute");
        result = stmt.executeQuery();

        while (result.next()) {
            if (!attributes.containsKey(result.getString("attribute_id"))) {
                attributes.put(result.getString("attribute_id"), new Attribute(result.getString("default_value") != null ? result.getString("default_value") : ""));
            }
        }

        return attributes;
    }

    static List<Question> getQuestions(Connection connection, long siteId) throws SQLException {
        List<Question> list = new LinkedList<>();

        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM valuate_question WHERE site_id=?");
        stmt.setLong(1, siteId);
        ResultSet result = stmt.executeQuery();

        while (result.next()) {
            list.add(new Question(result.getLong("site_id"), result.getString("question_id"),
                    result.getBoolean("lock"), result.getString("question_text"), result.getString("user_note"), getAttributes(connection, siteId, result.getString("question_id"))));
        }

        return list;
    }
}
