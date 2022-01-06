package valuate.api.feedback;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import valuate.api.attribute.Attribute;

public class Feedback {

    private final long feedbackId;
    private final long originId;
    private final String questionId;

    private final Date received;
    private final String valuatorIP;
    private final String valuatorUserAgent;

    private final Map<String, Attribute> attributes;

    // Made by the Endpoint servlet
    public Feedback(long originId, String questionId, String valuatorIP, String valuatorUserAgent, Map<String, Attribute> attributes) {
        this.feedbackId = -1;
        this.originId = originId;
        this.questionId = questionId;
        this.received = new Date(System.currentTimeMillis());
        this.valuatorIP = valuatorIP;
        this.valuatorUserAgent = valuatorUserAgent;
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    // Read from the database
    public Feedback(long feedbackId, long originId, String questionId, Date received, String valuatorIP, String valuatorUserAgent, Map<String, Attribute> attributes) {
        this.feedbackId = feedbackId;
        this.originId = originId;
        this.questionId = questionId;
        this.received = received;
        this.valuatorIP = valuatorIP;
        this.valuatorUserAgent = valuatorUserAgent;
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public long getFeedbackId() {
        return feedbackId;
    }

    public long getOriginId() {
        return originId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public Date getReceived() {
        return received;
    }

    public String getValuatorIP() {
        return valuatorIP;
    }

    public String getValuatorUserAgent() {
        return valuatorUserAgent;
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        String feedback = "";
        if (feedbackId > 0) {
            feedback += "ID: " + feedbackId + " ";
        }
        feedback += "\nQuestion ID: " + questionId;
        feedback += "\nFor: " + originId + " (" + received + ")";
        if (!attributes.isEmpty()) {
            feedback += "\nAttributes:";
            for (String af : attributes.keySet()) {
                feedback += "\n\"" + af + "\":\"" + attributes.get(af) + "\"";
            }
        }
        return feedback;
    }
}
