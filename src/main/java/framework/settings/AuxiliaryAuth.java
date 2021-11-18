package framework.settings;

import framework.diagnostics.MonitoredComponent;
import framework.diagnostics.Status;
import framework.diagnostics.Status.State;
import framework.user.HashCalculator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class AuxiliaryAuth extends MonitoredComponent {

    private static final Logger LOG = Logger.getLogger(AuxiliaryAuth.class.getName());
    private static final AuxiliaryAuth instance = new AuxiliaryAuth();

    private final String path = "/config/auxiliary-auth.xml";
    private final Map<String, String> userMap;

    private AuxiliaryAuth() {
        super("Auxiliary Login", true);
        userMap = new HashMap<>();
    }

    public static AuxiliaryAuth getInstance() {
        return instance;
    }

    private void parseDocument(InputStream istream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document adminDoc = builder.parse(istream);

        NodeList admins = adminDoc.getElementsByTagName("admin");
        for (int i = 0; i < admins.getLength(); i++) {
            String email = admins.item(i).getAttributes().getNamedItem("email").getTextContent();
            String passwordHash = admins.item(i).getAttributes().getNamedItem("password_hash").getTextContent();
            if (userMap.containsKey(email)) {
                LOG.log(Level.INFO, "Login {0} overriden.", email);
                userMap.remove(email);
            }
            userMap.put(email, passwordHash);
        }
    }

    @Override
    public void initialize() {
        try (InputStream istream = AuxiliaryAuth.class.getResourceAsStream(path);) {
            userMap.clear();

            parseDocument(istream);

            // Load Overrides
            try (InputStream overrideStream = new FileInputStream(new File(OverridePaths.getInstance().getAdminGods()))) {
                parseDocument(overrideStream);
                LOG.log(Level.INFO, "Overriding {0}:{1} -> {2}", new Object[]{"admin-gods.xml", path, OverridePaths.getInstance().getAdminGods()});
            } catch (IOException | ParserConfigurationException | SAXException | NullPointerException ex) {
                LOG.log(Level.INFO, "Failed to load override file ({0}).", "admin-gods.xml");
            }

            status = new Status(State.OPERATIONAL);
        } catch (IOException | ParserConfigurationException | SAXException | NullPointerException ex) {
            status = new Status(State.MALFUNCTION, ex);
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void shutdown() {
        userMap.clear();
        status = new Status(State.UNINITIALIZED);
    }

    public boolean authenticate(String username, String password) {
        LOG.log(Level.INFO, "Resorting to auxiliary authentication for {0}.", username);

        if (status.isOperational()) {
            return HashCalculator.sha512(password).equalsIgnoreCase(userMap.get(username));
        } else {
            return false;
        }
    }

    public String getPasswordHash(String username) {
        if (status.isOperational()) {
            return userMap.get(username);
        } else {
            return null;
        }
    }

    public boolean isGod(String username) {
        if (status.isOperational()) {
            return userMap.containsKey(username);
        } else {
            return false;
        }
    }

}
