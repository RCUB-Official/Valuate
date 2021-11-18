package framework.settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

class OverridePaths {

    private static final Logger LOG = Logger.getLogger(OverridePaths.class.getName());

    private static final OverridePaths instance = new OverridePaths();

    private final String path = "/config/overrides.properties";

    private String adminGods;
    private String databaseSettings;
    private String valuateSettings;

    private OverridePaths() {
        Properties properties = new Properties();
        try (InputStream istream = PropertiesHandler.class.getResourceAsStream(path);) {
            properties.load(istream);

            // TODO: read override paths
            adminGods = properties.getProperty("auxiliary-auth");
            databaseSettings = properties.getProperty("database");
            valuateSettings = properties.getProperty("valuate");

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static OverridePaths getInstance() {
        return instance;
    }

    // TODO: generate getters for all override paths
    public String getDatabaseSettings() {
        return databaseSettings;
    }

    public String getValuateSettings() {
        return valuateSettings;
    }

    public String getAdminGods() {
        return adminGods;
    }

}
