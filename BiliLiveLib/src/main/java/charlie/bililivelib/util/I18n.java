package charlie.bililivelib.util;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class I18n {
    public static final String BUNDLE_NAME = "BiliLiveLib";
    private static Map<String, String> strings;

    static {
        init();
    }

    private static void init() {
        strings = new HashMap<>();
        fillResourceBundle();
    }

    private static void fillResourceBundle() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
        for (String key : resourceBundle.keySet()) {
            strings.put(key, resourceBundle.getString(key));
        }
    }

    @Nls
    public static String getString(@NonNls @NotNull String key) {
        return strings.get(key);
    }

    @NotNull
    public static String format(@NonNls @NotNull String key,
                                @NonNls @NotNull Object... arguments) {
        return MessageFormat.format(strings.get(key), arguments);
    }
}
