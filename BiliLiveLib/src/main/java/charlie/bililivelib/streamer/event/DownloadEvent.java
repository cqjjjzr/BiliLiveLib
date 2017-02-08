package charlie.bililivelib.streamer.event;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nls;

import java.util.EventObject;

/**
 * 描述下载事件。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Getter
@ToString
public class DownloadEvent extends EventObject {
    private String message;
    private Kind kind;

    public DownloadEvent(Object source) {
        super(source);
        message = "";
        kind = Kind.OTHER;
    }

    public DownloadEvent(Object source, @Nls String message, Kind kind) {
        super(source);
        this.message = message;
        this.kind = kind;
    }

    public enum Kind {
        STARTING, STARTED, STOPPED, LIVE_STOPPED, ERROR, OTHER
    }
}
