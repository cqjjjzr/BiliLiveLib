package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.datamodel.WelcomeVipInfo;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 分发老爷进直播间事件的分发器。对COMMAND为'WELCOME'的事件有效。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class WelcomeVipDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMANDS = {
            "WELCOME"
    };
    private static WelcomeVipDispatcher GLOBAL_INSTANCE;

    @NotNull
    public static WelcomeVipDispatcher getGlobalInstance() {
        if (GLOBAL_INSTANCE == null) {
            GLOBAL_INSTANCE = new WelcomeVipDispatcher();
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
        DanmakuEvent event = new DanmakuEvent(source, new WelcomeVipInfo(rootObject),
                DanmakuEvent.Kind.WELCOME_VIP);
        for (DanmakuListener listener : listeners) {
            listener.welcomeVipEvent(event);
        }
    }
}
