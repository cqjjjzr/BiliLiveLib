package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.danmaku.event.DanmakuListener;

import java.util.List;

/**
 * 用于分发弹幕服务器事件的监听器。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public interface Dispatcher {
    /**
     * 尝试分发指定事件。
     * @param listeners 监听器列表
     * @param body 事件内容
     * @param source 事件发生源
     */
    void tryDispatch(List<DanmakuListener> listeners, String body, Object source);
}
