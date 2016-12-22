package charlie.bililivelib.danmaku;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.Globals;
import charlie.bililivelib.danmaku.datamodel.JoinServerJson;
import charlie.bililivelib.danmaku.dispatch.DanmakuPacket;
import charlie.bililivelib.danmaku.dispatch.DanmakuReceivePacket;
import charlie.bililivelib.danmaku.dispatch.DispatchManager;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import charlie.bililivelib.datamodel.Room;
import charlie.bililivelib.i18n.I18n;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static charlie.bililivelib.util.ByteArrayOperation.*;

public class DanmakuReceiver implements Runnable {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String[] CMT_SERVERS = {
            "livecmt-1.bilibili.com",
            "livecmt-2.bilibili.com"
    };
    public static final int CMT_PORT = 788;
    public static final int HEARTBEAT_PERIOD = 30 * 1000;
    public static final int RESPONSE_HEADER_SIZE = 16;

    @Getter
    private Room room;
    @Getter
    @Setter
    private long uid;
    @Getter
    @Setter
    private String commentServer;

    private List<DanmakuListener> listeners = new LinkedList<>();
    @Getter
    @Setter
    private DispatchManager dispatchManager = new DispatchManager();
    private Thread thread;
    private Timer heartbeatTimer;
    private volatile Status status = Status.NOT_CONNECTED;

    private OutputStream outputStream;
    private InputStream  inputStream;
    private Socket       socket;

    public DanmakuReceiver(Room room) {
        this(room, generateRandomUID(), CMT_SERVERS[0]);
    }

    public DanmakuReceiver(Room room, long uid) {
        this(room, uid, CMT_SERVERS[0]);
    }

    public DanmakuReceiver(Room room, long uid, String commentServer) {
        this.room = room;
        this.uid = uid;
        this.commentServer = commentServer;
    }

    public void connect() {
        if (status == Status.NOT_CONNECTED) {
            thread = new Thread(this);
            heartbeatTimer = new Timer("DanmakuReceiver-HeartbeatTimer-" + room.getRoomID());
            thread.start();
        }
    }

    public void disconnect() {
        heartbeatTimer.cancel();
        status = Status.NOT_CONNECTED;
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException ignored) {}
    }

    @Override
    public void run() {
        startupThread();
        try {
            socket = new Socket(commentServer, CMT_PORT);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            joinServer();

            status = Status.CONNECTED;
            fireDanmakuEvent(I18n.format("msg.danmaku_joined", room.getRoomID()), DanmakuEvent.Kind.JOINED);
            heartbeatTimer.schedule(new TimerTask() {
                @Override
                @SuppressWarnings("deprecation")
                public void run() {
                    try {
                        writeHeartbeat();
                    } catch (IOException e) {
                        fireDanmakuEvent(I18n.format("msg.danmaku_heartbeat_fail",
                                e.getClass().getName(), e.getMessage()), DanmakuEvent.Kind.ERROR);
                        disconnect();
                    }
                }
            }, 0, HEARTBEAT_PERIOD);

            while (status == Status.CONNECTED) {
                byte[] tempBuf = new byte[4];
                readArray(inputStream, tempBuf, 4);
                int length = byteArrayToInt(tempBuf);
                checkValidLength(length);

                readArray(inputStream, tempBuf, 2); // Magic Number

                byte[] shortBuf = new byte[2];
                readArray(inputStream, shortBuf, 2);
                short protocolVersion = byteArrayToShort(shortBuf);
                //checkValidProtocolVersion(protocolVersion);

                readArray(inputStream, tempBuf, 4);
                int operationID = byteArrayToInt(tempBuf);

                readArray(inputStream, tempBuf, 4); // Magic and params
                int bodyLength = length - RESPONSE_HEADER_SIZE;
                if (bodyLength == 0) continue;

                operationID -= 1; // I don't know what this means...

                DanmakuReceivePacket.Operation operation = DanmakuReceivePacket.Operation.forID(operationID);

                byte[] bodyBuffer = new byte[bodyLength];
                readArray(inputStream, bodyBuffer, bodyLength);

                dispatchPacket(operation, bodyBuffer);
            }
        } catch (Exception e) {
            if (status == Status.CONNECTED) {
                disconnect();
                fireDanmakuEvent(I18n.format("msg.danmaku_exception_down",
                        e.getClass().getName(), e.getMessage()), DanmakuEvent.Kind.ERROR);
            }
        }
    }

    private void dispatchPacket(DanmakuReceivePacket.Operation operation, byte[] bodyBuffer) {
        switch (operation) {
            case PLAYER_COUNT:
                int count = byteArrayToInt(bodyBuffer);
                fireDanmakuEvent(count, DanmakuEvent.Kind.WATCHER_COUNT);
                break;
            case UNKNOWN:
            case PLAYER_COMMAND:
                dispatchManager.dispatch(listeners, new String(bodyBuffer), this);
        }
    }

    private void checkValidProtocolVersion(short version) throws BiliLiveException {
        if (version != 1)
            throw new BiliLiveException(I18n.format("msg.danmaku_protocol_error"));
    }

    private void checkValidLength(int length) throws BiliLiveException {
        if (length < 16)
            throw new BiliLiveException(I18n.format("msg.danmaku_protocol_error"));
    }

    private void startupThread() {
        Thread.currentThread().setName("DanmakuReceiver-" + room.getRoomID());
    }

    private void joinServer() throws IOException {
        JoinServerJson json = new JoinServerJson(room.getRoomID(), uid);
        writePacket(new DanmakuPacket(DanmakuPacket.Action.JOIN_SERVER,
                Globals.get().getGson().toJson(json)));
    }

    private void writeHeartbeat() throws IOException {
        writePacket(new DanmakuPacket(DanmakuPacket.Action.HEARTBEAT));
    }

    private void writePacket(DanmakuPacket packet) throws IOException {
        outputStream.write(packet.generate());
    }

    private int readArray(InputStream stream, byte[] buffer, int length) throws IOException {
        if (length > buffer.length)
            throw new IOException("offset + length > buffer.length");
        int readLength = 0;

        while (readLength < length) {
            int available = stream.read(buffer, 0, length - readLength);
            if (available == 0)
                throw new IOException("available == 0");
            readLength += available;
        }
        return readLength;
        //return stream.read(buffer, 0, length);
    }

    private static long generateRandomUID() {
        return (long) (1e14 + 2e14 * Math.random());
    }

    public void addDanmakuListener(DanmakuListener listener) {
        listeners.add(listener);
    }

    public void removeDanmakuListener(DanmakuListener listener) {
        listeners.remove(listener);
    }

    private void fireDanmakuEvent(Object param, DanmakuEvent.Kind kind) {
        DanmakuEvent event = new DanmakuEvent(this, param, kind);
        switch (kind) {
            case ERROR:
                for (DanmakuListener listener : listeners) {
                    listener.errorEvent(event);
                }
                break;
            case WATCHER_COUNT:
                for (DanmakuListener listener : listeners) {
                    listener.watcherCountEvent(event);
                }
                break;
            case JOINED:
                for (DanmakuListener listener : listeners) {
                    listener.statusEvent(event);
                }
                break;
        }

    }

    public enum Status {
        CONNECTED, NOT_CONNECTED
    }
}
