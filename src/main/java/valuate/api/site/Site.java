package valuate.api.site;

import framework.EventHandler;
import framework.utilities.Utilities;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.validator.UrlValidator;

public final class Site {

    private static final Logger LOG = Logger.getLogger(Site.class.getName());

    private final long id;

    private String name;
    private boolean spamProtect;

    private final Date created;
    private Date modified;

    private final List<String> prefixes;
    private String toBeAddedPrefix = "";

    Site(long id, String name, boolean spamProtect, Date created, Date modified, List<String> prefixes) {
        this.id = id;
        this.name = name;
        this.spamProtect = spamProtect;
        this.created = created;
        this.modified = modified;
        this.prefixes = prefixes;
    }

    public long getId() {
        return id;
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

    public void delete() {
        SiteServer.deleteSite(id);
        SiteController sc = (SiteController) Utilities.getObject("#{siteController}");
        if (sc != null) {
            sc.init();
        }
    }
    /*
    public String getSnippet() {
        return "<script async defer src=\"" + ValuateSettings.getInstance().getUrl() + "/script?for=" + id + "\" onLoad=\"valuateLoad()\"></script>\n"
                + "<div id=\"valuate\"" + " lowest=\"" + lowest + "\" highest=\"" + highest + " emoji=\"bw\"" + "\n\t"
                + " user-logo=\"" + userLogo + "\" user-link=\"" + userLink + "\"" + ">\n"
                + "\t<div id=\"valuate_question\">" + question + "</div>\n"
                + "</div>";
    }
     */
}
