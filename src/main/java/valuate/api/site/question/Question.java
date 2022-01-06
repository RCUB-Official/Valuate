package valuate.api.site.question;

import valuate.api.attribute.Attribute;
import framework.settings.ValuateSettings;
import framework.utilities.Utilities;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import valuate.api.attribute.AttributeField;
import valuate.api.attribute.AttributeServer;
import valuate.api.site.SiteController;

public class Question implements Serializable {

    private final long siteId;
    private String questionId;

    private final Date created;
    private Date modified;

    private boolean lock;
    private String userNote;

    private String oldQuestionId;

    private final Map<String, Attribute> attributes;

    Question(long siteId, String questionId, boolean lock, String userNote, Date created, Date modified, Map<String, Attribute> attributes) {
        this.siteId = siteId;
        this.questionId = questionId;
        this.lock = lock;
        this.userNote = userNote;
        this.created = created;
        this.modified = modified;

        this.attributes = attributes;

        this.oldQuestionId = questionId;
    }

    public long getSiteId() {
        return siteId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public boolean isUpdateBySnippet() {
        return !lock;
    }

    public void setUpdateBySnippet(boolean updateBySnippet) {
        this.lock = !updateBySnippet;
        QuestionServer.setQuestionLock(siteId, questionId, lock);
    }

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public Date getCreated() {
        return created;
    }

    public Date getModified() {
        return modified;
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

    public void updateAttributes() {
        QuestionServer.updateQuestion(siteId, oldQuestionId, questionId, lock, userNote, attributes);
        modified = new Date(System.currentTimeMillis());
        oldQuestionId = questionId;
    }

    public String getSnippet() {
        ValuateSettings settings = ValuateSettings.getInstance();
        String snippet = "<script async defer src=\"" + settings.getUrl() + "/script?for=" + siteId + "\" onLoad=\"valuateLoad()\"></script>"
                + "\n<valuate id=\"" + questionId + "\"";

        int breakCounter = 1;
        for (String key : attributes.keySet()) {
            AttributeField field = AttributeServer.getAttributeField(key);
            if (field != null) {
                if (field.isInSnippetEditor()) {
                    Attribute attribute = attributes.get(key);
                    if (!attribute.isEmpty() && !attribute.getFieldId().equals(settings.getQuestionAttributeFieldId())) {
                        if (breakCounter++ % 4 == 0) {
                            snippet += "\n";
                        } else {
                            snippet += " ";
                        }
                        snippet += attribute.getFieldId() + "=\"" + attribute.getValue() + "\"";
                    }
                }
            }
        }
        snippet += ">" + attributes.get(settings.getQuestionAttributeFieldId()) + "</valuate>";

        return snippet;
    }

    public void delete() {
        QuestionServer.deleteQuestion(siteId, questionId);
        SiteController sc = (SiteController) Utilities.getObject("#{siteController}");
        if (sc != null) {
            sc.getSite().reloadQuestions();
        }
    }
}
