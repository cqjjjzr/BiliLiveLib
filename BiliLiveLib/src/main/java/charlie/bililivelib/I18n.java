package charlie.bililivelib;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 多语言环境工具类。
 *
 * @author Charlie Jiang
 * @since 1.8 rv1
 */
public class I18n {
    public static final String BUNDLE_NAME = "BiliLiveLib";
    private static Map<String, String> strings;

    static {
        init();
    }

    /**
     * 重新依照当前Locale获取并填充本地化字符串。在切换Locale后调用。
     */
    public static void init() {
        strings = new HashMap<>();
        fillResourceBundle();
    }

    private static void fillResourceBundle() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
        for (String key : resourceBundle.keySet()) {
            strings.put(key, resourceBundle.getString(key));
        }
    }

    /**
     * 获取给定Key的本地化文本。
     * @param key 键
     * @return 本地化文本
     */
    @Nls
    public static String getString(@NonNls @NotNull String key) {
        return strings.get(key);
    }

    /**
     * 获取基于给定Key的本地化格式化文本。
     * @param key 指向格式化模式的键
     * @param arguments 格式化文本的参数
     * @return 本地化并格式化后的文本
     */
    @NotNull
    public static String format(@NonNls @NotNull String key,
                                @NonNls @NotNull Object... arguments) {
        return MessageFormat.format(strings.get(key), arguments);
    }
}
