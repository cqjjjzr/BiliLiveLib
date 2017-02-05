package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class DispatchManager {
    private List<Dispatcher> dispatchers = new LinkedList<>();

    public void registerDispatcher(@NotNull Dispatcher dispatcher) {
        dispatchers.add(dispatcher);
    }

    public void unregisterDispatcher(@NotNull Dispatcher dispatcher) {
        dispatchers.remove(dispatcher);
    }

    public void dispatch(@NotNull List<DanmakuListener> listeners, @NotNull String body, @NotNull Object source) {
        for (Dispatcher dispatcher : dispatchers) {
            try {
                dispatcher.tryDispatch(listeners, body, source);
            } catch (Exception ex) {
                DanmakuEvent event = new DanmakuEvent(dispatcher, ex, DanmakuEvent.Kind.ERROR);
                for (DanmakuListener listener : listeners) {
                    listener.errorEvent(event);
                }
            }
        }
    }

    public boolean isDispatcherPresented(@NotNull Class<?> clazz) {
        for (Dispatcher dispatcher : dispatchers) {
            if (dispatcher.getClass().isAssignableFrom(clazz)) return true;
        }
        return false;
    }
}
