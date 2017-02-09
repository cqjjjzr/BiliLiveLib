package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.Globals;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;

import java.util.List;

/**
 * 对{@link Dispatcher}的抽象实现，用于事件内容是JSON文本时的处理。
 */
public abstract class AbstractJSONDispatcher implements Dispatcher {
    /**
     * 获取可以接受的事件命令。
     *
     * @return 事件命令列表
     */
    protected abstract String[] getAcceptableCommands();

    @Contract(pure = true)
    private boolean isValid(String body) {
        return body.startsWith("{");
    }

    private boolean checkCanDispatch(JsonObject rootObject) {
        String command = getCommand(rootObject);
        for (String acceptable : getAcceptableCommands()) {
            if (command.equals(acceptable)) return true;
        }
        return false;
    }

    /**
     * 检查指定事件是否为JSON对象，且是否能被本监听器处理。
     * 如果可以则交由{@link AbstractJSONDispatcher#dispatch(List, JsonObject, Object)}进行分发。
     * @param listeners 监听器列表
     * @param body 事件内容
     * @param source 事件发生源
     */
    @Override
    public void tryDispatch(List<DanmakuListener> listeners, String body, Object source) {
        if (!isValid(body)) return;
        JsonObject rootObject = Globals.get().gson().fromJson(body, JsonObject.class);
        if (!checkCanDispatch(rootObject))
            return;

        dispatch(listeners, rootObject, source);
    }

    public String getCommand(JsonObject rootObject) {
        return rootObject.get("cmd").getAsString();
    }

    /**
     * 分发指定事件。
     * @param listeners 监听器列表
     * @param rootObject 事件内容产生的JSON对象结构
     * @param source 事件发生源
     */
    public abstract void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source);
}
