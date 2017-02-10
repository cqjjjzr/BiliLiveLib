package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.datamodel.StartStopInfo;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 分发直播间状态改变事件的分发器。对COMMAND为'LIVE', 'PREPARING'和'ROUND'的事件有效。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class StartStopDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
            "LIVE", "PREPARING", "ROUND"
    };
    private static StartStopDispatcher GLOBAL_INSTANCE;

    @NotNull
    public static StartStopDispatcher getGlobalInstance() {
        if (GLOBAL_INSTANCE == null) {
            GLOBAL_INSTANCE = new StartStopDispatcher();
        }
        return GLOBAL_INSTANCE;
    }

    @Override
    protected String[] getAcceptableCommands() {
        return ACCEPTABLE_COMMANDS;
    }

    /**
     * {@inheritDoc}
     * @param listeners 监听器列表
     * @param rootObject 事件内容产生的JSON对象结构
     * @param source 事件发生源
     */
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
