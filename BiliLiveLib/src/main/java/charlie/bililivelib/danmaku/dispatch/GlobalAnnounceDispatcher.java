package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.Globals;
import charlie.bililivelib.danmaku.datamodel.GlobalAnnounceInfo;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;

import java.util.List;

public class GlobalAnnounceDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
            "SYS_MSG"
    };

    @Override
    protected String[] getAcceptableCommands() {
        return ACCEPTABLE_COMMANDS;
    }

    @Override
    public void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source) {
        if (rootObject.has("tv_id")) return; //SMALL TV.

        GlobalAnnounceInfo announce = Globals.get().gson()
                .fromJson(rootObject, GlobalAnnounceInfo.class);
        DanmakuEvent event = new DanmakuEvent(source, announce, DanmakuEvent.Kind.GLOBAL_ANNOUNCE);
        for(DanmakuListener listener : listeners) {
            listener.globalAnnounceEvent(event);
        }
    }
}
