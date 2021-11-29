package valuate;

import java.util.Date;

public class MockReport {

    private final Date received;
    private final int grade;

    private String comment;
    private String country;

    private String ipAddress;
    private String userAgent;
    private String username;

    public MockReport(Date received, int grade) {
        this.received = received;
        this.grade = grade;
    }

    public Date getReceived() {
        return received;
    }

    public int getGrade() {
        return grade;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
