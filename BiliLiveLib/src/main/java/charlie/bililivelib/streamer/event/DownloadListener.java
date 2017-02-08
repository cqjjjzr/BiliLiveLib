package charlie.bililivelib.streamer.event;

/**
 * 用于监听下载器事件的监听器。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public interface DownloadListener {
    /**
     * 在发生事件时调用。
     * @param event 事件对象
     */
    void downloadEvent(DownloadEvent event);
}
