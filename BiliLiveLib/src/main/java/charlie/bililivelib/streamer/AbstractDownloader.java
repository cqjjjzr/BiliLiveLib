package charlie.bililivelib.streamer;

import charlie.bililivelib.I18n;
import charlie.bililivelib.room.Room;
import charlie.bililivelib.streamer.event.DownloadEvent;
import charlie.bililivelib.streamer.event.DownloadListener;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * 抽象的流下载器。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public abstract class AbstractDownloader {
    protected URL liveURL;
    protected Room room;
    @Getter
    protected File path;

    @Getter
    protected Status status = Status.STOPPED;
    @Getter
    protected String message;

    private List<DownloadListener> listeners = new LinkedList<>();

    protected String generateErrorMessage(@NotNull Throwable e) {
        return I18n.format("msg.exception.download", e.getClass().getName(), e.getMessage());
    }

    public void setPath(File path) {
        if (status == Status.STOPPED)
            this.path = path;
    }

    protected void reportException(Throwable e) {
        status = Status.ERROR;
        message = generateErrorMessage(e);
    }

    protected void startupThread() {
        Thread.currentThread().setName(this.getClass().getSimpleName() + "-" + room.getRoomID());
    }

    /**
     * 开始下载流。
     */
    public abstract void start();

    /**
     * 尝试停止下载流。
     */
    public abstract void tryStop();

    /**
     * 强制停止下载流。
     */
    public abstract void forceStop();

    /**
     * 注册下载监听器。
     *
     * @param listener 监听器
     */
    public void addDownloadListener(DownloadListener listener) {
        listeners.add(listener);
    }

    /**
     * 解除注册下载监听器。
     *
     * @param listener 监听器
     */
    public void removeDownloadListener(DownloadListener listener) {
        listeners.remove(listener);
    }

    protected void fireDownloadEvent(String message, DownloadEvent.Kind kind) {
        DownloadEvent event = new DownloadEvent(this, message, kind);
        for (DownloadListener listener :
                listeners) {
            listener.downloadEvent(event);
        }
    }

    /**
     * 描述下载器的状态。
     */
    public enum Status {
        STARTING, STARTED, STOPPING, STOPPED, ERROR
    }
}
