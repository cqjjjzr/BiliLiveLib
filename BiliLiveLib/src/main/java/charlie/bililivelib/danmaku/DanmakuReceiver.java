package charlie.bililivelib.danmaku;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.GlobalObjects;
import charlie.bililivelib.danmaku.datamodel.JoinServerJson;
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
    public static final int _30_SECOND = 30 * 1000;
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
    private Thread thread;
    private Timer heartbeatTimer;
    private volatile Status status = Status.NOT_CONNECTED;

    private OutputStream outputStream;
    private InputStream  inputStream;

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
        if (status == Status.NOT_CONNECTED){
            thread = new Thread(this);
            heartbeatTimer = new Timer("DanmakuReceiver-HeartbeatTimer-" + room.getRoomID(), true);
            thread.start();
        }
    }

    public void disconnect() {
        heartbeatTimer.cancel();
        status = Status.NOT_CONNECTED;
        //TODO Safe disconnect
    }

    @Override
    public void run() {
        startupThread();
        try {
            Socket socket = new Socket(commentServer, CMT_PORT);
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
            }, _30_SECOND);

            while (status == Status.CONNECTED) {
                byte[] tempBuf = new byte[4];

                readArray(inputStream, tempBuf, 4);
                int length = byteArrayToInt(tempBuf);
                checkValidLength(length);

                readArray(inputStream, tempBuf, 2); // Magic Number
                readArray(inputStream, tempBuf, 2);
                short protocolVersion = byteArrayToShort(tempBuf);
                checkValidProtocolVersion(protocolVersion);

                readArray(inputStream, tempBuf, 4);
                int operationID = byteArrayToInt(tempBuf);

                readArray(inputStream, tempBuf, 4); // Magic and params

                int bodyLength = length - RESPONSE_HEADER_SIZE;
                if (bodyLength == 0) continue;

                operationID -= 1; // I don't know what this means...

                DanmakuReceivePacket.Operation operation = DanmakuReceivePacket.Operation.forID(operationID);
            }
        } catch (Exception e) {
            if (status == Status.CONNECTED) {
                disconnect();
                fireDanmakuEvent(I18n.format("msg.danmaku_exception_down",
                        e.getClass().getName(), e.getMessage()), DanmakuEvent.Kind.ERROR);
            }
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
                GlobalObjects.instance().getGson().toJson(json)));
    }

    private void writeHeartbeat() throws IOException {
        writePacket(new DanmakuPacket(DanmakuPacket.Action.HEARTBEAT));
    }

    private void writePacket(DanmakuPacket packet) throws IOException {
        outputStream.write(packet.generate());
    }

    private void readArray(InputStream stream, byte[] buffer, int length) throws IOException {
        if (length > buffer.length)
            throw new IOException("offset + length > buffer.length");
        int readLength = 0;

        while (readLength < length) {
            int available = stream.read(buffer, 0, length - readLength);
            if (available == 0 || available == -1)
                throw new IOException("available == 0");
            readLength += available;
        }
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

    private void fireDanmakuEvent(String message, DanmakuEvent.Kind kind) {
        DanmakuEvent event = new DanmakuEvent(this, message, kind);
        for (DanmakuListener listener : listeners) {
            listener.danmakuEvent(event);
        }
    }

    public enum Status {
        CONNECTED, NOT_CONNECTED
    }
}
