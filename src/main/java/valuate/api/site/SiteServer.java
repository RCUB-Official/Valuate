package valuate.api.site;

import framework.database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SiteServer {

    private static final Logger LOG = Logger.getLogger(SiteServer.class.getName());

    public static long addNewSite(int userId, String siteName, String urlPrefix) {
        long siteId = -1;

        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false);

            PreparedStatement stmt = connection.prepareStatement("INSERT INTO valuate_site(user_id, site_name) VALUES(?, ?) RETURNING site_id");
            stmt.setInt(1, userId);
            stmt.setString(2, siteName);
            stmt.execute();

            ResultSet keys = stmt.getResultSet();
            if (keys.next()) {
                siteId = keys.getLong("site_id");
                stmt = connection.prepareStatement("INSERT INTO site_url_prefix(site_id, url_prefix) VALUES(?, ?)");
                stmt.setLong(1, siteId);
                stmt.setString(2, urlPrefix);
                stmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException | InterruptedException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFail) {
                    LOG.log(Level.SEVERE, null, rollbackFail);
                }
            }
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }

        return siteId;
    }

    public static void deleteSite(long siteId) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM valuate_site WHERE site_id=?");
            stmt.setLong(1, siteId);
            stmt.executeUpdate();

        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
    }

    private static List<String> getSitePrefixes(long siteId, Connection connection) throws SQLException {
        List<String> prefixes = new LinkedList<>();

        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM site_url_prefix WHERE site_id=? ORDER BY url_prefix ASC");
        stmt.setLong(1, siteId);
        ResultSet result = stmt.executeQuery();
        while (result.next()) {
            prefixes.add(result.getString("url_prefix"));
        }

        return prefixes;
    }

    public static Site getSite(long id) {
        Site site = null;
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM valuate_site WHERE site_id=?");
            stmt.setLong(1, id);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                site = new Site(id, result.getInt("user_id"), result.getString("site_name"), result.getBoolean("spam_protect"),
                        new Date(result.getTimestamp("created").getTime()), new Date(result.getTimestamp("modified").getTime()),
                        getSitePrefixes(id, connection));
                site.setQuestions(QuestionServer.getQuestions(connection, id));
            }
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
        return site;
    }

    public static List<Site> getSitesForUser(int userId) {
        List<Site> sites = new LinkedList<>();
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM valuate_site WHERE user_id=?");
            stmt.setInt(1, userId);
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                Site site = new Site(result.getLong("site_id"), result.getInt("user_id"), result.getString("site_name"), result.getBoolean("spam_protect"),
                        new Date(result.getTimestamp("created").getTime()), new Date(result.getTimestamp("modified").getTime()),
                        getSitePrefixes(result.getLong("site_id"), connection));
                site.setQuestions(QuestionServer.getQuestions(connection, site.getId()));
                sites.add(site);
            }
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
        return sites;
    }

    static void addPrefix(long siteId, String prefix) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO site_url_prefix(site_id, url_prefix) VALUES (?, ?); "
                    + "UPDATE valuate_site SET modified=current_timestamp WHERE site_id=?");
            stmt.setLong(1, siteId);
            stmt.setString(2, prefix);
            stmt.setLong(3, siteId);

            stmt.executeUpdate();
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
    }

    static void deletePrefix(long siteId, String prefix) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM site_url_prefix WHERE site_id=? AND url_prefix=?; "
                    + "UPDATE valuate_site SET modified=current_timestamp WHERE site_id=?");
            stmt.setLong(1, siteId);
            stmt.setString(2, prefix);
            stmt.setLong(3, siteId);

            stmt.executeUpdate();
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
    }

    static void changeName(long siteId, String name) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            PreparedStatement stmt = connection.prepareStatement("UPDATE valuate_site SET site_name=?, modified=current_timestamp WHERE site_id=?");
            stmt.setString(1, name);
            stmt.setLong(2, siteId);
            stmt.executeUpdate();
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
    }

    static void changeSpamProtect(long siteId, boolean spamProtect) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        try {
            connection = pool.getConnection();
            PreparedStatement stmt = connection.prepareStatement("UPDATE valuate_site SET spam_protect=?, modified=current_timestamp WHERE site_id=?");
            stmt.setBoolean(1, spamProtect);
            stmt.setLong(2, siteId);
            stmt.executeUpdate();
        } catch (SQLException | InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            pool.returnConnection(connection);
        }
    }

}
