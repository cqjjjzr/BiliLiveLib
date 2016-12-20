package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.event.DanmakuListener;

import java.util.LinkedList;
import java.util.List;

public class DispatchManager {
    private List<Dispatcher> dispatchers = new LinkedList<>();

    public void registerDispatcher(Dispatcher dispatcher) {
        dispatchers.add(dispatcher);
    }

    public void unregisterDispatcher(Dispatcher dispatcher) {
        dispatchers.remove(dispatcher);
    }

    public void dispatch(List<DanmakuListener> listeners, String body, Object source) {
        for (Dispatcher dispatcher : dispatchers) {
            dispatcher.tryDispatch(listeners, body, source);
        }
    }
}
