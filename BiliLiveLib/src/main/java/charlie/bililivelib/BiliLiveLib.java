package charlie.bililivelib;

import charlie.bililivelib.util.I18n;

public class BiliLiveLib {
    public static final String PROJECT_NAME = "BiliLiveLib";

    public static final String VERSION = "rv1-debug";
    public static String USER_AGENT = PROJECT_NAME + " " + VERSION;

    public void init() {
        I18n.init();
    }
}
