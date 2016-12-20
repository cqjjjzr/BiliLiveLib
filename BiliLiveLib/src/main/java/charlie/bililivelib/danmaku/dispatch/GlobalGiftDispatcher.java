package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;

import java.util.List;

public class GlobalGiftDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
            "SYS_GIFT"
    };

    @Override
    protected String[] getAcceptableCommands() {
        return new String[0];
    }

    @Override
    public void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source) {

    }
}
