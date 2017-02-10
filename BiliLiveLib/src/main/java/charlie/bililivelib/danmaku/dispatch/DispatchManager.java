package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * 用于管理分发器的管理器。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class DispatchManager {
    private List<Dispatcher> dispatchers = new LinkedList<>();

    /**
     * 注册新的分发器。
     * @param dispatcher 分发器
     */
    public void registerDispatcher(@NotNull Dispatcher dispatcher) {
        dispatchers.add(dispatcher);
    }

    /**
     * 注销指定分发器。
     * @param dispatcher 分发器
     */
    public void unregisterDispatcher(@NotNull Dispatcher dispatcher) {
        dispatchers.remove(dispatcher);
    }

    /**
     * 尝试分发信息。
     * @param listeners 监听器列表
     * @param body 信息体
     * @param source 事件源
     */
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

    /**
     * 检查本管理器是否有指定类型的分发器。
     * @param clazz 类型
     * @return 是否含有
     */
    public boolean isDispatcherPresented(@NotNull Class<?> clazz) {
        for (Dispatcher dispatcher : dispatchers) {
            if (dispatcher.getClass().isAssignableFrom(clazz)) return true;
        }
        return false;
    }
}
