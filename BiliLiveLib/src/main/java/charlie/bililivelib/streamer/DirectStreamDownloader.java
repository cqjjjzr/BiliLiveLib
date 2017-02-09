package charlie.bililivelib.streamer;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.I18n;
import charlie.bililivelib.internalutil.net.HttpHelper;
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
public class DirectStreamDownloader extends AbstractThreadBasedDownloader {
    private static final int _1_KB = 1024;

    public DirectStreamDownloader(URL liveURL, Room room, File path) {
        this(liveURL, room, path, BiliLiveLib.DEFAULT_USER_AGENT);
    }

    public DirectStreamDownloader(URL liveURL, Room room, File path, String userAgent) {
        this.liveURL = liveURL;
        this.room = room;
        this.path = path;
        this.userAgent = userAgent;
    }

    @Override
    protected void download() {
        try {
            status = Status.STARTING;
            eventStarting();
            HttpResponse response = new HttpHelper().init(userAgent)
                    .createGetResponse(liveURL);

            if (!path.exists()) path.createNewFile();
            try (OutputStream fileStream = Files.newOutputStream(path.toPath(), StandardOpenOption.TRUNCATE_EXISTING);
                 InputStream liveStream = HttpHelper.responseToInputStream(response)
            ) {
                eventStarted();
                int readLength;
                byte[] buffer = new byte[_1_KB];
                while ((readLength = liveStream.read(buffer, 0, _1_KB)) != -1 &&
                        status != Status.STOPPING) {
                    fileStream.write(buffer, 0, readLength);
                }
                eventStopped();
                status = Status.STOPPED;
            }
        } catch (Exception e) {
            reportException(e);
        }
    }

    private void eventStarting() {
        fireDownloadEvent(I18n.getString("msg.stream_starting"), DownloadEvent.Kind.STARTING);
    }

    private void eventStarted() {
        fireDownloadEvent(I18n.format("msg.stream_started",
                path.getAbsolutePath()), DownloadEvent.Kind.STARTED);
    }

    private void eventStopped() {
        if (status == Status.STARTED) { // Exits normally
            fireDownloadEvent(I18n.format("msg.stream_stopped",
                    path.getAbsoluteFile()), DownloadEvent.Kind.STOPPED);
        } else if (status == Status.STOPPING) { //Exits manually
            fireDownloadEvent(I18n.format("msg.stream_manually_stopped",
                    path.getAbsoluteFile()), DownloadEvent.Kind.STOPPED);
        }
    }
}
