package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.Globals;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import charlie.bililivelib.smalltv.SmallTVRoom;
import com.google.gson.JsonObject;

import java.util.List;

public class GlobalGiftDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
            "SYS_MSG"
    };

    @Override
    protected String[] getAcceptableCommands() {
        return ACCEPTABLE_COMMANDS;
    }

    @Override
    public void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source) {
        if (!rootObject.has("tv_id")) return; //NOT A SMALL TV OBJECT.

        SmallTVRoom smallTV = Globals.get().getGson()
                .fromJson(rootObject, SmallTVRoom.class);
        DanmakuEvent event = new DanmakuEvent(source, smallTV, DanmakuEvent.Kind.GLOBAL_GIFT);
        for(DanmakuListener listener : listeners) {
            listener.globalGiftEvent(event);
        }
    }
}
