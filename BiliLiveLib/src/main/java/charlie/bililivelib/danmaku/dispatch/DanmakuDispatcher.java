package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.datamodel.Danmaku;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;

import java.util.List;

public class DanmakuDispatcher extends AbstractJSONDispatcher {
    public static final String[] ACCEPTABLE_COMMENDS = {
            "DANMU_MSG"
    };

    @Override
    protected String[] getAcceptableCommands() {
        return ACCEPTABLE_COMMENDS;
    }

    @Override
    public void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source) {
        Danmaku danmaku = new Danmaku(rootObject);

        DanmakuEvent event = new DanmakuEvent(source, danmaku, DanmakuEvent.Kind.NEW_DANMAKU);
        for(DanmakuListener listener : listeners) {
            listener.danmakuEvent(event);
        }
    }
}
