package valuate.api.site;

import valuate.api.site.question.Question;
import framework.EventHandler;
import framework.utilities.Utilities;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.apache.commons.validator.UrlValidator;
import valuate.api.site.question.QuestionServer;

public final class Site implements Serializable {

    private final long id;
    private final int ownerId;

    private String name;
    private boolean spamProtect;

    private final Date created;
    private Date modified;

    private final List<String> prefixes;
    private String toBeAddedPrefix = "";

    private List<Question> questions = null;

    Site(long id, int ownerId, String name, boolean spamProtect, Date created, Date modified, List<String> prefixes) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.spamProtect = spamProtect;
        this.created = created;
        this.modified = modified;
        this.prefixes = prefixes;
    }

    public long getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSpamProtect() {
        return spamProtect;
    }

    public void setSpamProtect(boolean spamProtect) {
        this.spamProtect = spamProtect;
        SiteServer.changeSpamProtect(id, this.spamProtect);
        modified = new Date(System.currentTimeMillis());
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public Date getCreated() {
        return created;
    }

    public Date getModified() {
        return modified;
    }

    public void deletePrefix(String prefix) {
        for (String p : prefixes) {
            if (p.equals(prefix)) {
                prefixes.remove(p);
                break;
            }
        }
        SiteServer.deletePrefix(id, prefix);
        modified = new Date(System.currentTimeMillis());
    }

    public String getToBeAddedPrefix() {
        return toBeAddedPrefix;
    }

    public void setToBeAddedPrefix(String toBeAddedPrefix) {
        this.toBeAddedPrefix = toBeAddedPrefix;
    }

    public void addPrefix() {
        UrlValidator validator = new UrlValidator();
        if (validator.isValid(toBeAddedPrefix)) {
            boolean duplicate = false;
            for (String p : prefixes) {
                if (p.equals(toBeAddedPrefix)) {
                    duplicate = true;
                    EventHandler.alertUserError("Prefix already exists", toBeAddedPrefix);
                    toBeAddedPrefix = "";
                    break;
                }
            }
            if (!duplicate && !"".equals(toBeAddedPrefix)) {
                prefixes.add(toBeAddedPrefix);
                SiteServer.addPrefix(id, toBeAddedPrefix);
                modified = new Date(System.currentTimeMillis());
                toBeAddedPrefix = "";
            }
        } else {
            EventHandler.alertUserError("Invalid url", toBeAddedPrefix);
        }
    }

    public void changeName() {
        if ("".equals(name)) {
            EventHandler.alertUserError("Unable to save changes", "Site name and description must not be empty.");
        } else {
            SiteServer.changeName(id, name);
            modified = new Date(System.currentTimeMillis());
        }
    }

    public List<Question> getQuestions() {
        if (questions == null) {
            questions = QuestionServer.getQuestions(id);
        }
        return questions;
    }

    public void reloadQuestions() {
        this.questions = QuestionServer.getQuestions(id);
    }

    public void delete() {
        SiteServer.deleteSite(id);
        SiteController sc = (SiteController) Utilities.getObject("#{siteController}");
        if (sc != null) {
            sc.init();
        }
    }

}
