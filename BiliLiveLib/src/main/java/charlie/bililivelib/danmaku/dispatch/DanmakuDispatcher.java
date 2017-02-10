package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.datamodel.Danmaku;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 分发用户弹幕的分发器。对COMMAND为'DANMU_MSG'的事件有效。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class DanmakuDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMENDS = {
            "DANMU_MSG"
    };
    private static DanmakuDispatcher GLOBAL_INSTANCE;

    @NotNull
    public static DanmakuDispatcher getGlobalInstance() {
        if (GLOBAL_INSTANCE == null) {
            GLOBAL_INSTANCE = new DanmakuDispatcher();
        }
        return GLOBAL_INSTANCE;
    }

    @Override
    protected String[] getAcceptableCommands() {
        return ACCEPTABLE_COMMENDS;
    }

    /**
     * {@inheritDoc}
     * @param listeners 监听器列表
     * @param rootObject 事件内容产生的JSON对象结构
     * @param source 事件发生源
     */
    @Override
    public void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source) {
        Danmaku danmaku = new Danmaku(rootObject);

        DanmakuEvent event = new DanmakuEvent(source, danmaku, DanmakuEvent.Kind.NEW_DANMAKU);
        for(DanmakuListener listener : listeners) {
            listener.danmakuEvent(event);
        }
    }
}
