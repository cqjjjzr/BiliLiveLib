package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.GlobalObjects;
import charlie.bililivelib.danmaku.datamodel.Danmaku;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;

import java.util.List;

public class DanmakuDispatcher implements Dispatcher {
    public static void init() {
        DispatchManager.instance().registerDispatcher(new DanmakuDispatcher());
    }

    @Override
    public void tryDispatch(List<DanmakuListener> listeners, String body) {
        if (!isValid(body)) return;
        JsonObject rootObject = GlobalObjects.instance().getGson().fromJson(body, JsonObject.class);
        if (!checkCanDispatch(rootObject)) return;
        Danmaku danmaku = new Danmaku(rootObject);

        DanmakuEvent event = new DanmakuEvent(this, danmaku, DanmakuEvent.Kind.NEW_DANMAKU);
        for(DanmakuListener listener : listeners) {
            listener.danmakuEvent(event);
        }
    }

    private boolean isValid(String body) {
        return body.startsWith("{");
    }

    private boolean checkCanDispatch(JsonObject rootObject) {
        return rootObject.get("cmd").getAsString().equals("DANMU_MSG");
    }
}
