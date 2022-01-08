package framework.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class ValuateSettings extends Settings {

    private static final ValuateSettings instance = new ValuateSettings();

    private final String scriptPath = "/config/client-script.js";

    private String clientScript;
    private String url;

    private String questionAttributeFieldId;
    private String questionIdAutoPrefix;

    private int attributeFieldCacheSize;

    private ValuateSettings() {
        super("valuate", "/config/configuration.properties", "Valuate Settings", true);
    }

    public static ValuateSettings getInstance() {
        return instance;
    }

    @Override
    public void load() throws IOException {
        PropertiesHandler handler = new PropertiesHandler(prefix, path, OverridePaths.getInstance().getValuateSettings());

        url = handler.getParam("url");
        questionAttributeFieldId = handler.getParam("questionAttributeFieldId");
        questionIdAutoPrefix = handler.getParam("questionIdAutoPrefix");
        attributeFieldCacheSize = Integer.parseInt(handler.getParam("attributeFieldCacheSize"));

        // Loading client-script
        try (InputStream templateIstream = ValuateSettings.class.getResourceAsStream(scriptPath)) {
            InputStreamReader isr = new InputStreamReader(templateIstream, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            // Setting constants
            clientScript = "const valuateServiceUrl=\"" + url + "\";\n";
            clientScript += "const autoPrefix=\"" + questionIdAutoPrefix + "\";";

            String line;
            while ((line = br.readLine()) != null) {
                clientScript += line + "\n";
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public String getClientScript() {
        return clientScript;
    }

    public String getQuestionAttributeFieldId() {
        return questionAttributeFieldId;
    }

    public String getQuestionIdAutoPrefix() {
        return questionIdAutoPrefix;
    }

    public int getAttributeFieldCacheSize() {
        return attributeFieldCacheSize;
    }

}
