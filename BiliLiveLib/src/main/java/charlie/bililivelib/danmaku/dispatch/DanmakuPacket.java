package charlie.bililivelib.danmaku.dispatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static charlie.bililivelib.danmaku.DanmakuReceiver.UTF8;

@Data
@AllArgsConstructor
public class DanmakuPacket {
    private static final short DEFAULT_MAGIC = 16;
    private static final short DEFAULT_PROTOCOL_VERSION = 1;
    private static final int DEFAULT_PARAM = 1;
    private static final int DEFAULT_LENGTH = -1;

    private short magic = DEFAULT_MAGIC;
    private short protocolVersion = DEFAULT_PROTOCOL_VERSION;
    private int length;
    private Action action;
    private int param = DEFAULT_PARAM;
    private String body = "";

    public DanmakuPacket(Action action) {
        this(action, "");
    }

    public DanmakuPacket(Action action, String body) {
        this(DEFAULT_MAGIC,
                DEFAULT_PROTOCOL_VERSION,
                DEFAULT_LENGTH,
                action,
                DEFAULT_PARAM,
                body);
    }

    public byte[] generate() {
        byte[] bodyBytes = body.getBytes(UTF8);

        calculateLength(bodyBytes);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(length);
        writeBigEndian(length,          byteArrayOutputStream);
        writeBigEndian(magic,           byteArrayOutputStream);
        writeBigEndian(protocolVersion, byteArrayOutputStream);
        writeBigEndian(action.getID(),  byteArrayOutputStream);
        writeBigEndian(param,           byteArrayOutputStream);
        if (bodyBytes.length > 0) {
            byteArrayOutputStream.write(bodyBytes, 0, bodyBytes.length);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private void writeBigEndian(short data, ByteArrayOutputStream stream) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.SIZE / 8);
        buffer.asShortBuffer().put(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        stream.write(buffer.array(), 0, 2);
    }

    private void writeBigEndian(int data, ByteArrayOutputStream stream) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / 8);
        buffer.asIntBuffer().put(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        stream.write(buffer.array(), 0, 4);
    }

    private void calculateLength(byte[] bytes) {
        if (length == -1)
            length = bytes.length + 16;
    }

    public enum Action {
        HEARTBEAT(2), JOIN_SERVER(7);

        Action(int ID) {
            this.ID = ID;
        }

        @Getter
        private int ID;
    }
}
