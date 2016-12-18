package charlie.bililivelib.danmaku.event;

import lombok.Getter;

import java.util.EventObject;

@Getter
public class DanmakuEvent extends EventObject {
    private Kind kind;
    private Object param;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public DanmakuEvent(Object source) {
        super(source);
    }

    public DanmakuEvent(Object source, Object param, Kind kind) {
        super(source);
        this.kind = kind;
        this.param = param;
    }

    public enum Kind {
        JOINED, ERROR, NEW_DANMAKU, WATCHER_COUNT, START_STOP
    }
}
