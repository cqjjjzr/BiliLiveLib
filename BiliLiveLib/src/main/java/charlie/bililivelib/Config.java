package charlie.bililivelib;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class Config {
    private XMLConfiguration configuration;

    public void init() throws ConfigurationException {
        configuration = new Configurations().xmlBuilder("BiliRecorderConfig.xml").getConfiguration();
    }

    public String getString(String key, String defaultValue) {
        return configuration.getString("config." + key, defaultValue);
    }

    public String getString(String key) {
        return getString(key, "");
    }
}
