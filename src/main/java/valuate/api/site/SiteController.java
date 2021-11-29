package valuate.api.site;

import framework.EventHandler;
import framework.user.User;
import framework.utilities.Utilities;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name = "siteController", eager = true)
@ViewScoped
public class SiteController implements Serializable {

    int userId = -1;

    List<Site> sites = null;

    Site site = null;

    private String toBeAddedSiteName;
    private String toBeAddedUrlPrefix;

    public SiteController() {
    }

    @PostConstruct
    public void init() {
        User user = (User) Utilities.getObject("#{user}");
        userId = user.getId();

        try {   // Try specific site by ID
            site = SiteServer.getSite(Long.parseLong((String) Utilities.getObject("#{param.site_id}")));
        } catch (NumberFormatException ex) {    // List sites for a user
            if (userId != -1) {
                sites = SiteServer.getSitesForUser(userId);
            }
        }
    }

    public List<Site> getSites() {
        return sites;
    }

    public Site getSite() {
        return site;
    }

    // Add new site
    public String getToBeAddedSiteName() {
        return toBeAddedSiteName;
    }

    public void setToBeAddedSiteName(String toBeAddedSiteName) {
        this.toBeAddedSiteName = toBeAddedSiteName;
    }

    public String getToBeAddedUrlPrefix() {
        return toBeAddedUrlPrefix;
    }

    public void setToBeAddedUrlPrefix(String toBeAddedUrlPrefix) {
        this.toBeAddedUrlPrefix = toBeAddedUrlPrefix;
    }

    public String addNewSite() {
        User user = (User) Utilities.getObject("#{user}");
        long siteId = SiteServer.addNewSite(user.getId(), toBeAddedSiteName, toBeAddedUrlPrefix);
        if (siteId != -1) {
            sites = SiteServer.getSitesForUser(userId);
            return "site?site_id=" + siteId + "&faces-redirect=true";
        } else {
            EventHandler.alertUserError("Failed to add a site", "Check the logs to debug this issue.");
            return "index";
        }
    }

}
