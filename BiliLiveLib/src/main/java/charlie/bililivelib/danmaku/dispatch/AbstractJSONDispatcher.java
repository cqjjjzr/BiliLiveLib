package charlie.bililivelib.danmaku.dispatch;

import charlie.bililivelib.Globals;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import com.google.gson.JsonObject;

import java.util.List;

public abstract class AbstractJSONDispatcher implements Dispatcher {
    protected abstract String[] getAcceptableCommands();

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

    @Override
    public void tryDispatch(List<DanmakuListener> listeners, String body, Object source) {
        if (!isValid(body)) return;
        JsonObject rootObject = Globals.get().getGson().fromJson(body, JsonObject.class);
        if (!checkCanDispatch(rootObject))
            return;

        dispatch(listeners, rootObject, source);
    }

    public String getCommand(JsonObject rootObject) {
        return rootObject.get("cmd").getAsString();
    }

    public abstract void dispatch(List<DanmakuListener> listeners, JsonObject rootObject, Object source);
}
