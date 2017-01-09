package charlie.bililivelib.streamer.event;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nls;

import java.util.EventObject;

@Getter
@ToString
public class DownloadEvent extends EventObject {
    private String message;
    private Kind kind;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
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
