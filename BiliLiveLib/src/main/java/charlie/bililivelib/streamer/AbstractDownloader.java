package charlie.bililivelib.streamer;

import charlie.bililivelib.datamodel.Room;
import charlie.bililivelib.i18n.I18n;
import lombok.Getter;

import java.io.File;
import java.net.URL;

public abstract class AbstractDownloader {
    protected URL liveURL;
    protected Room room;
    @Getter
    protected File path;

    @Getter
    protected Status status = Status.STOPPED;
    @Getter
    protected String message;

    public enum Status {
        STARTING, STARTED, STOPPING, STOPPED, ERROR
    }

    protected String generateErrorMessage(Throwable e) {
        return I18n.format("msg.exception.download", e.getClass().getName(), e.getMessage());
    }

    public void setPath(File path) {
        if (status == Status.STOPPED)
            this.path = path;
    }

    protected void reportError(Throwable e) {
        status = Status.ERROR;
        message = generateErrorMessage(e);
    }

    protected void startupThread() {
        Thread.currentThread().setName(this.getClass().getSimpleName() + "-" + room.getRoomID());
    }

    public abstract void start();

    public abstract void tryStop();

    public abstract void forceStop();
}
