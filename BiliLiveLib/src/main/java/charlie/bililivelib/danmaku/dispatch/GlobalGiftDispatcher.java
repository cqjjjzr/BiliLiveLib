package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.Globals;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import charlie.bililivelib.smalltv.SmallTV;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 分发小电视事件的分发器。对COMMAND为'SYS_MSG'且含有'tv_id'值的事件有效。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class GlobalGiftDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
            "SYS_MSG"
    };
    private static GlobalGiftDispatcher GLOBAL_INSTANCE;

    @NotNull
    public static GlobalGiftDispatcher getGlobalInstance() {
        if (GLOBAL_INSTANCE == null) {
            GLOBAL_INSTANCE = new GlobalGiftDispatcher();
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
        if (!rootObject.has("tv_id")) return; //NOT A SMALL TV OBJECT.

        SmallTV smallTV = Globals.get().gson()
                .fromJson(rootObject, SmallTV.class);
        DanmakuEvent event = new DanmakuEvent(source, smallTV, DanmakuEvent.Kind.GLOBAL_GIFT);
        for(DanmakuListener listener : listeners) {
            listener.globalGiftEvent(event);
        }
    }
}
