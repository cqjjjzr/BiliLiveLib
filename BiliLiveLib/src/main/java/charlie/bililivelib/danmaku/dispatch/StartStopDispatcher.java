package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.datamodel.StartStopInfo;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;

import java.util.List;

public class StartStopDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
            "LIVE", "PREPARING", "ROUND"
    };

    @Override
    protected String[] getAcceptableCommands() {
        return ACCEPTABLE_COMMANDS;
    }

    @Override
    public void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source) {
        boolean living = getCommand(rootObject).equals("LIVE");
        int roomID = rootObject.get("roomid").getAsInt();

        DanmakuEvent event = new DanmakuEvent(source, new StartStopInfo(roomID, living),
                DanmakuEvent.Kind.START_STOP);
        for (DanmakuListener listener : listeners) {
            listener.startStopEvent(event);
        }
    }
}
