package valuate.api.site;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Question {

    private final long siteId;
    private String questionId;
    private boolean lock;
    private String questionText;
    private String userNote;

    private final Map<String, Attribute> attributes;

    Question(long siteId, String questionId, boolean lock, String questionText, String userNote, Map<String, Attribute> attributes) {
        this.siteId = siteId;
        this.questionId = questionId;
        this.lock = lock;
        this.questionText = questionText;
        this.userNote = userNote;
        this.attributes = attributes;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public Attribute getAttribute(String attributeId) {
        return attributes.get(attributeId);
    }

    public List<String> getAttributes() {
        List<String> list = new LinkedList<>();

        for (String key : attributes.keySet()) {
            list.add(key);
        }

        return list;
    }

}
