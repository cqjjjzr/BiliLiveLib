package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.GlobalObjects;
import charlie.bililivelib.danmaku.datamodel.GiveGiftInfo;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;

import java.util.List;

public class GiveGiftDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
        "SEND_GIFT"
    };

    @Override
    protected String[] getAcceptableCommands() {
        return ACCEPTABLE_COMMANDS;
    }

    @Override
    public void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source) {
        GiveGiftInfo giveGiftInfo = GlobalObjects.instance().getGson()
                .fromJson(rootObject, GiveGiftInfo.class);

        DanmakuEvent event = new DanmakuEvent(source, giveGiftInfo, DanmakuEvent.Kind.GIVE_GIFT);
        for (DanmakuListener listener : listeners) {
            listener.giveGiftEvent(event);
        }
    }
}
