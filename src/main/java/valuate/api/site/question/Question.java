package valuate.api.site.question;

public class Question {

    private final long siteId;
    private String questionId;
    private boolean lock;
    private String questionText;

    // For Snippet
    private String lowest;
    private String highest;
    private String emojiSetId;
    private String userLogoUrl;
    private String userUrl;

    Question(long siteId, String questionId, boolean lock, String questionText, String lowest, String highest, String emojiSetId, String userLogoUrl, String userUrl) {
        this.siteId = siteId;
        this.questionId = questionId;
        this.lock = lock;
        this.questionText = questionText;
        this.lowest = lowest;
        this.highest = highest;
        this.emojiSetId = emojiSetId;
        this.userLogoUrl = userLogoUrl;
        this.userUrl = userUrl;
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

    public String getLowest() {
        return lowest;
    }

    public void setLowest(String lowest) {
        this.lowest = lowest;
    }

    public String getHighest() {
        return highest;
    }

    public void setHighest(String highest) {
        this.highest = highest;
    }

    public String getEmojiSetId() {
        return emojiSetId;
    }

    public void setEmojiSetId(String emojiSetId) {
        this.emojiSetId = emojiSetId;
    }

    public String getUserLogoUrl() {
        return userLogoUrl;
    }

    public void setUserLogoUrl(String userLogoUrl) {
        this.userLogoUrl = userLogoUrl;
    }

    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

}
