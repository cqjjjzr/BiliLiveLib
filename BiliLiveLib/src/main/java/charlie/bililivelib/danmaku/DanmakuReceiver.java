package charlie.bililivelib.danmaku;

import charlie.bililivelib.Globals;
import charlie.bililivelib.I18n;
import charlie.bililivelib.danmaku.datamodel.JoinServerJson;
import charlie.bililivelib.danmaku.dispatch.DispatchManager;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.danmaku.event.DanmakuListener;
import charlie.bililivelib.room.Room;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static charlie.bililivelib.internalutil.ByteArrayOperation.byteArrayToInt;

/**
 * 本类用于处理Bilibili Live弹幕服务器的通信。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class DanmakuReceiver implements Runnable {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * 目前可用的弹幕服务器。
     */
    public static final String[] CMT_SERVERS = {
            "livecmt-1.bilibili.com",
            "livecmt-2.bilibili.com"
    };
    /** 弹幕服务器端口 */
    public static final int CMT_PORT = 788;
    /**
     * 心跳包发送周期，以ms为单位
     */
    public static final int HEARTBEAT_PERIOD_MILLIS = 30 * 1000;

    @Getter
    private Room room;
    @Getter
    private long uid;
    @Getter
    private String commentServer;

    private List<DanmakuListener> listeners = new LinkedList<>();
    @Getter
    private DispatchManager dispatchManager = new DispatchManager();
    private Timer heartbeatTimer;
    private volatile Status status = Status.NOT_CONNECTED;

    private OutputStream outputStream;
    private InputStream  inputStream;
    private Socket       socket;

    public DanmakuReceiver(@NotNull Room room) {
        this(room, generateRandomUID(), CMT_SERVERS[0]);
    }

    public DanmakuReceiver(@NotNull Room room, long uid) {
        this(room, uid, CMT_SERVERS[0]);
    }

    public DanmakuReceiver(@NotNull Room room, long uid, @NotNull String commentServer) {
        this.room = room;
        this.uid = uid;
        this.commentServer = commentServer;

        checkArguments();
    }

    /**
     * 生成随机UID。
     * @return UID
     */
    public static long generateRandomUID() {
        return (long) (1e14 + 2e14 * Math.random());
    }

    private void checkArguments() {
        if (room == null || commentServer == null) throw new NullPointerException();
        if (uid < 1) throw new IllegalArgumentException("UserID < 0");
        if (commentServer.isEmpty()) throw new IllegalArgumentException("commentServer is empty");
    }

    /**
     * 连接到弹幕服务器。
     */
    public void connect() {
        if (status == Status.NOT_CONNECTED) {
            Thread thread = new Thread(this);
            heartbeatTimer = new Timer("DanmakuReceiver-HeartbeatTimer-" + room.getRoomID());
            thread.start();
        }
    }

    /**
     * 从弹幕服务器断开。
     */
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
            }, 0, HEARTBEAT_PERIOD_MILLIS);

            while (status == Status.CONNECTED) {
                DanmakuReceivePacket packet = new DanmakuReceivePacket(inputStream);
                if (packet.getBody() == null) continue;

                dispatchPacket(packet.getOperation(), packet.getBody());
            }
        } catch (Exception e) {
            if (status == Status.CONNECTED) {
                fireDanmakuEvent(e, DanmakuEvent.Kind.ERROR_DOWN);
                disconnect();
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

    private void startupThread() {
        Thread.currentThread().setName("DanmakuReceiver-" + room.getRoomID());
    }

    private void joinServer() throws IOException {
        JoinServerJson json = new JoinServerJson(room.getRoomID(), uid);
        writePacket(new DanmakuPacket(DanmakuPacket.Action.JOIN_SERVER,
                Globals.get().gson().toJson(json)));
    }

    private void writeHeartbeat() throws IOException {
        writePacket(new DanmakuPacket(DanmakuPacket.Action.HEARTBEAT));
    }

    private void writePacket(DanmakuPacket packet) throws IOException {
        outputStream.write(packet.generate());
    }

    /**
     * 增加弹幕服务器事件监听器。
     * @param listener 监听器
     */
    public void addDanmakuListener(DanmakuListener listener) {
        listeners.add(listener);
    }

    /**
     * 删除弹幕服务器事件监听器。
     * @param listener 监听器
     */
    public void removeDanmakuListener(DanmakuListener listener) {
        listeners.remove(listener);
    }

    /**
     * 检查是否有给定弹幕服务器事件监听器。
     * @param listener 监听器
     * @return 结果
     */
    public boolean hasDanmakuListener(DanmakuListener listener) {
        return listeners.contains(listener);
    }

    private void fireDanmakuEvent(Object param, DanmakuEvent.Kind kind) {
        DanmakuEvent event = new DanmakuEvent(this, param, kind);
        switch (kind) {
            case ERROR_DOWN:
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

    /**
     * 设置通信类的事件分发器管理器。
     * @param dispatchManager 分发器管理器
     */
    public void setDispatchManager(@NotNull DispatchManager dispatchManager) {
        this.dispatchManager = dispatchManager;
    }

    public void setCommentServer(@NotNull String commentServer) {
        if (commentServer.isEmpty()) throw new IllegalArgumentException("commentServer is empty");
        this.commentServer = commentServer;
    }

    /**
     * 设置UID。
     * @param uid UID，要求<0
     */
    public void setUid(long uid) {
        if (uid < 1) throw new IllegalArgumentException("uid < 1");
        this.uid = uid;
    }

    public enum Status {
        CONNECTED, NOT_CONNECTED
    }
}
