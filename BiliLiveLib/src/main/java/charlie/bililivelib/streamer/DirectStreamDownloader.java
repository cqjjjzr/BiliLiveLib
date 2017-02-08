package charlie.bililivelib.streamer;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.I18n;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.room.Room;
import charlie.bililivelib.streamer.event.DownloadEvent;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * 通过直接下载Http流进行保存的下载器，未压缩，但处理最快。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class DirectStreamDownloader extends AbstractDownloader implements Runnable {
    private static final int _1_KB = 1024;

    private InputStream stream;

    private Thread thread;
    private String userAgent;

    public DirectStreamDownloader(URL liveURL, Room room, File path) {
        this(liveURL, room, path, BiliLiveLib.DEFAULT_USER_AGENT);
    }

    public DirectStreamDownloader(URL liveURL, Room room, File path, String userAgent) {
        this.liveURL = liveURL;
        this.room = room;
        this.path = path;
        this.userAgent = userAgent;
    }

    /**
     * {@inheritDoc}
     */
    public void start() {
        if (status != Status.STOPPED && status != Status.ERROR) return;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * {@inheritDoc}
     */
    public void tryStop() {
        status = Status.STOPPING;

        fireDownloadEvent(I18n.getString("msg.try_stopping"), DownloadEvent.Kind.STOPPED);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public void forceStop() {
        thread.stop();
        status = Status.STOPPED;
        fireDownloadEvent(I18n.getString("msg.force_stopped"), DownloadEvent.Kind.STOPPED);
    }

    @Override
    public void run() {
        try {
            status = Status.STARTING;
            fireDownloadEvent(I18n.getString("msg.stream_starting"), DownloadEvent.Kind.STARTING);
            HttpResponse response = new HttpHelper().init(userAgent)
                    .createGetResponse(liveURL);

            if (!path.exists()) path.createNewFile();
            try (OutputStream fileStream = Files.newOutputStream(path.toPath(), StandardOpenOption.TRUNCATE_EXISTING);
                 InputStream liveStream = HttpHelper.responseToInputStream(response)
            ) {
                fireDownloadEvent(I18n.format("msg.stream_started",
                        path.getAbsolutePath()), DownloadEvent.Kind.STARTED);
                int readLength;
                byte[] buffer = new byte[_1_KB];
                while ((readLength = liveStream.read(buffer, 0, _1_KB)) != -1) {
                    if (status == Status.STOPPING) break;
                    fileStream.write(buffer, 0, readLength);
                }
                if (status == Status.STARTED) { // Exits normally
                    fireDownloadEvent(I18n.format("msg.stream_stopped",
                            path.getAbsoluteFile()), DownloadEvent.Kind.STOPPED);
                } else if (status == Status.STOPPING) {
                    fireDownloadEvent(I18n.format("msg.stream_manually_stopped",
                            path.getAbsoluteFile()), DownloadEvent.Kind.STOPPED);
                }
                status = Status.STOPPED;
            }
        } catch (Exception e) {
            reportException(e);
        }
    }
}
