package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.event.DanmakuListener;

import java.util.LinkedList;
import java.util.List;

public class DispatchManager {
    private static DispatchManager instance;

    private List<Dispatcher> dispatchers = new LinkedList<>();

    public void registerDispatcher(Dispatcher dispatcher) {
        dispatchers.add(dispatcher);
    }

    public void dispatch(List<DanmakuListener> listeners, String body) {
        for (Dispatcher dispatcher : dispatchers) {
            dispatcher.tryDispatch(listeners, body);
        }
    }

    public static DispatchManager instance() {
        if (instance == null)
            instance = new DispatchManager();
        return instance;
    }

    private DispatchManager() {}
}
