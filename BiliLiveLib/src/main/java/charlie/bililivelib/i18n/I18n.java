package charlie.bililivelib.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class I18n {
    private static Map<String, String> strings;

    public static void init() {
        strings = new HashMap<>();
        fillResourceBundle();
    }

    private static void fillResourceBundle() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("BiliLiveLib");
        for (String key : resourceBundle.keySet()) {
            strings.put(key, resourceBundle.getString(key));
        }
    }

    public static String getString(String key) {
        return strings.get(key);
    }

    public static String format(String key, Object... arguments) {
        return MessageFormat.format(strings.get(key), arguments);
    }
}
