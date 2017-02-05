package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.Globals;
import charlie.bililivelib.danmaku.datamodel.GiveGiftInfo;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GiveGiftDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
        "SEND_GIFT"
    };
    private static GiveGiftDispatcher GLOBAL_INSTANCE;

    @NotNull
    public static GiveGiftDispatcher getGlobalInstance() {
        if (GLOBAL_INSTANCE == null) {
            GLOBAL_INSTANCE = new GiveGiftDispatcher();
        }
        return GLOBAL_INSTANCE;
    }

    @Override
    protected String[] getAcceptableCommands() {
        return ACCEPTABLE_COMMANDS;
    }

    @Override
    public void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source) {
        GiveGiftInfo giveGiftInfo = Globals.get().gson()
                .fromJson(rootObject, GiveGiftInfo.class);

        DanmakuEvent event = new DanmakuEvent(source, giveGiftInfo, DanmakuEvent.Kind.GIVE_GIFT);
        for (DanmakuListener listener : listeners) {
            listener.giveGiftEvent(event);
        }
    }
}
