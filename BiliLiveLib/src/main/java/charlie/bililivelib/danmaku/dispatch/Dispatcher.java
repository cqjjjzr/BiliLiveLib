package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.event.DanmakuListener;

import java.util.List;

public interface Dispatcher {
    void tryDispatch(List<DanmakuListener> listeners, String body, Object source);
}
