package valuate;

import java.util.Date;

public class Feedback {

    private final long feedbackId;
    private final long originId;
    private final String questionId;

    private final Date received;
    private final String fullUrl;
    private final String question;
    private final String lowest;
    private final String highest;
    private final long grade;
    private final String comment;

    private final String valuatorId;
    private final String reference;

    // Made by the Endpoint servlet
    public Feedback(long originId, String questionId, String fullUrl, String question, String lowest, String highest, long grade, String comment, String valuatorId, String reference) {
        this.feedbackId = -1;
        this.originId = originId;
        this.questionId = questionId;
        this.received = new Date(System.currentTimeMillis());

        this.fullUrl = fullUrl;
        this.question = question;
        this.lowest = lowest;
        this.highest = highest;
        this.grade = grade;
        this.comment = comment;
        this.valuatorId = valuatorId;
        this.reference = reference;
    }

    // Read from the database
    public Feedback(long feedbackId, long originId, String questionId, Date received, String fullUrl, String question, String lowest, String highest, long grade, String comment, String valuatorId, String reference) {
        this.feedbackId = feedbackId;
        this.originId = originId;
        this.questionId = questionId;
        this.received = received;
        this.fullUrl = fullUrl;
        this.question = question;
        this.lowest = lowest;
        this.highest = highest;
        this.grade = grade;
        this.comment = comment;
        this.valuatorId = valuatorId;
        this.reference = reference;
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

    public String getFullUrl() {
        return fullUrl;
    }

    public String getQuestion() {
        return question;
    }

    public String getLowest() {
        return lowest;
    }

    public String getHighest() {
        return highest;
    }

    public long getGrade() {
        return grade;
    }

    public String getComment() {
        return comment;
    }

    public String getValuatorId() {
        return valuatorId;
    }

    public String getReference() {
        return reference;
    }

    @Override
    public String toString() {
        String feedback = "";
        if (feedbackId > 0) {
            feedback += "ID: " + feedbackId + " ";
        }
        feedback += "\nQuestion ID: " + questionId;
        feedback += "\nFor: " + originId + " (" + received + ")";
        feedback += "\nFrom: " + fullUrl;
        feedback += "\nQuestion: \"" + question + "\"";
        feedback += "\n" + lowest + "<-- " + grade + " -->" + highest;
        feedback += "\nComment: \"" + comment + "\"";
        if (valuatorId != null) {
            feedback += "\nBy: " + valuatorId;
        }
        feedback += "\nReference: " + reference;

        return feedback;
    }
}
