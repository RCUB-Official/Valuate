package framework.user;

import framework.settings.AuxiliaryAuth;
import framework.EventHandler;
import framework.database.ConnectionPool;
import framework.utilities.Utilities;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.RandomStringUtils;

@ManagedBean(name = "user", eager = true)
@SessionScoped
public class User implements Serializable {

    private static final Logger LOG = Logger.getLogger(User.class.getName());

    private int id;
    private String username;
    private String password;
    private String name;

    private Date registered;

    private boolean enabled;
    private boolean administrator;

    boolean authenticated;

    private void reset() {
        this.id = -1;
        this.authenticated = false;
        this.enabled = false;
        this.administrator = false;
    }

    public User() {
        reset();
    }

    public void givenUsingApache_whenGeneratingRandomAlphabeticString_thenCorrect() {
        String generatedString = RandomStringUtils.randomAlphabetic(10);
    }

    private void auxiliaryAuthentication() {
        AuxiliaryAuth godServer = AuxiliaryAuth.getInstance();
        boolean godAuthenticated = godServer.authenticate(username, password);
        if (godAuthenticated) {
            authenticated = enabled = administrator = true;
        }
    }

    public String login() {
        ConnectionPool pool = ConnectionPool.getInstance();
        if (pool.getStatus().isOperational()) { // Regular authentication
            Connection connection = null;
            try {
                connection = pool.getConnection();
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM valuate_user WHERE email=?");
                stmt.setString(1, username);
                ResultSet result = stmt.executeQuery();
                if (result.next()) {
                    id = result.getInt("user_id");
                    authenticated = result.getString("password_hash").equalsIgnoreCase(HashCalculator.sha512(password + result.getString("password_salt")));
                    if (authenticated) {
                        enabled = result.getBoolean("enabled");
                        if (enabled) {
                            administrator = result.getBoolean("administrator");
                            name = result.getString("name");

                            registered = new Date(result.getTimestamp("registered").getTime());

                            EventHandler.alertUserInfo("Welcome!", "User " + username + " authenticated.");

                            stmt = connection.prepareStatement("UPDATE valuate_user SET last_login=current_timestamp WHERE email=?");
                            stmt.setString(1, username);
                            stmt.executeUpdate();

                        } else {
                            reset();
                            EventHandler.alertUserError("Access denied", "Account " + username + " has been disabled.");
                        }
                    } else {
                        reset();
                        EventHandler.alertUserError("Access denied", "Wrong password for the user " + username + ".");
                    }
                } else {
                    auxiliaryAuthentication();
                    if (authenticated) {    // Sync with the auxiliary authentication
                        stmt = connection.prepareStatement("INSERT INTO valuate_user(email, name, password_hash, password_salt, last_login, email_verified, enabled, administrator)"
                                + " VALUES(?, '', ?, '', current_timestamp, true, true, true) RETURNING user_id");
                        stmt.setString(1, username);
                        stmt.setString(2, AuxiliaryAuth.getInstance().getPasswordHash(username));
                        stmt.execute();

                        ResultSet keys = stmt.getResultSet();
                        id = keys.next() ? keys.getInt("user_id") : -1;
                    } else {
                        EventHandler.alertUserError("User not found", username + " does not exist in our database.");
                    }
                }
            } catch (SQLException | InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                pool.returnConnection(connection);
            }
        } else { // Malfunction authentication
            auxiliaryAuthentication();
        }

        return "index?faces-redirect=true";
    }

    public String logout() {
        reset();
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        session.invalidate();
        EventHandler.alertUserInfo("Session terminated by user.", "Bye!");
        return "index?faces-redirect=true";
    }

    public int getId() {
        return id;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getRegistered() {
        return registered;
    }

    public boolean isGod() {
        return authenticated && AuxiliaryAuth.getInstance().isGod(username);
    }

    //Context-depentend static getters
    public static String getContextUsername() {
        User current_user = (User) Utilities.getObject("#{user}");
        return current_user.getUsername();
    }

    public static boolean isContextAuthenticated() {
        User current_user = (User) Utilities.getObject("#{user}");
        return current_user.isAuthenticated();
    }

}
