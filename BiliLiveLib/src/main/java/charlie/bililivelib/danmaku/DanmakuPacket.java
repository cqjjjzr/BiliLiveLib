package charlie.bililivelib.danmaku;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static charlie.bililivelib.danmaku.DanmakuReceiver.UTF8;

/**
 * 用于封装发送到弹幕服务器的数据包。<br />
 * 数据包的结构如下:<br />
 * {@code +-----+-------+-----+--------+-------+----------+}<br />
 * {@code | LEN | MAGIC | VER | OPCODE | PARAM |   BODY   |}<br />
 * {@code |  4  | X'0F' |  2  |   4    |   4   | Variable |}<br />
 * {@code +-----+-------+-----+--------+-------+----------+}<br />
 * - LEN:    数据包长度，包含自身长度，int类型;<br />
 * - MAGIC:  魔数，short类型，固定0x0F;<br />
 * - VER:    协议版本，short类型，目前仅支持0x01;<br />
 * - OPCODE: 操作码，int类型。具体见{@link DanmakuPacket.Action};<br />
 * - PARAM:  操作参数，int类型，默认0x01;<br />
 * - BODY:   数据包体，长度可变。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Data
@AllArgsConstructor
public final class DanmakuPacket {
    private static final short DEFAULT_MAGIC = 0x0F;
    private static final short DEFAULT_PROTOCOL_VERSION = 1;
    private static final int DEFAULT_PARAM = 1;
    private static final int DEFAULT_LENGTH = -1;

    private static final int PACKET_HEADER_SIZE = 16;

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

    /**
     * 由本类生成byte[]数组
     * @return byte[]数组
     */
    public byte[] generate() {
        byte[] bodyBytes = body.getBytes(UTF8);

        calculateLength(bodyBytes);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(length);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        writeBigEndian(length, dataOutputStream);
        writeBigEndian(magic, dataOutputStream);
        writeBigEndian(protocolVersion, dataOutputStream);
        writeBigEndian(action.getID(), dataOutputStream);
        writeBigEndian(param, dataOutputStream);
        if (bodyBytes.length > 0) {
            byteArrayOutputStream.write(bodyBytes, 0, bodyBytes.length);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private void writeBigEndian(short data, DataOutputStream stream) {
        /*
        ByteBuffer buffer = ByteBuffer.allocate(Short.SIZE / 8);
        buffer.asShortBuffer().put(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        stream.write(buffer.array(), 0, 2);*/
        try {
            stream.writeShort(data);
        } catch (IOException ignored) {
        }
        // Because the default endian of Java is Big-Endian, so we just directly write the data.
    }

    private void writeBigEndian(int data, DataOutputStream stream) {
        /*
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / 8);
        buffer.asIntBuffer().put(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        stream.write(buffer.array(), 0, 4);*/
        try {
            stream.writeInt(data);
        } catch (IOException ignored) {
        }
        // Because the default endian of Java is Big-Endian, so we just directly write the data.
    }

    private void calculateLength(byte[] bytes) {
        if (length == -1)
            length = bytes.length + PACKET_HEADER_SIZE;
    }

    /**
     * 标志Opcode。
     */
    public enum Action {
        HEARTBEAT(2), JOIN_SERVER(7);

        @Getter
        private int ID;

        Action(int ID) {
            this.ID = ID;
        }
    }
}
