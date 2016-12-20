package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.datamodel.WelcomeVipInfo;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;

import java.util.List;

public class WelcomeVipDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
            "WELCOME"
    };

    @Override
    protected String[] getAcceptableCommands() {
        return ACCEPTABLE_COMMANDS;
    }

    @Override
    public void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source) {
        DanmakuEvent event = new DanmakuEvent(source, new WelcomeVipInfo(rootObject),
                DanmakuEvent.Kind.WELCOME_VIP);
        for (DanmakuListener listener : listeners) {
            listener.welcomeVipEvent(event);
        }
    }
}
