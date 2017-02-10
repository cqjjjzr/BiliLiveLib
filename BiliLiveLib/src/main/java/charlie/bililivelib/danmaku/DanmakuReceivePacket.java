package charlie.bililivelib.danmaku;

import charlie.bililivelib.I18n;
import charlie.bililivelib.exceptions.BiliLiveException;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;

import static charlie.bililivelib.internalutil.ByteArrayOperation.byteArrayToInt;
import static charlie.bililivelib.internalutil.ByteArrayOperation.byteArrayToShort;

/**
 * 用于从弹幕服务器发回的数据包。<br>
 * 数据包的结构如下:<br>
 * {@code +-----+-------+-----+--------+-------+----------+}<br>
 * {@code | LEN | MAGIC | UNK | OPCODE | B.LEN | --BODY-- |}<br>
 * {@code | -4- | X'0F' | -2- | --4--- | --4-- | Variable |}<br>
 * {@code +-----+-------+-----+--------+-------+----------+}<br>
 * - LEN:    数据包长度，包含自身长度，int类型;<br>
 * - MAGIC:  魔数，short类型，固定0x0F;<br>
 * - UNK:    未知参数，int类型;<br>
 * - OPCODE: 操作码，int类型。具体见{@link DanmakuReceivePacket.Operation};<br>
 * - B.LEN:  BODY的长度，int类型;<br>
 * - BODY:   数据包体，长度可变。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Getter
public final class DanmakuReceivePacket {
    private static final int PACKET_HEADER_SIZE = 16;

    private Operation operation = null;
    private byte[] body = null;

    /**
     * 对于指定输入流，读取并构造数据包结构。
     *
     * @param inputStream 输入流
     * @throws IOException       发生网络错误时抛出
     * @throws BiliLiveException 协议错误时抛出
     */
    public DanmakuReceivePacket(InputStream inputStream) throws IOException, BiliLiveException {
        byte[] tempBuf = new byte[4];
        readArray(inputStream, tempBuf, 4);
        int length = byteArrayToInt(tempBuf);
        checkValidLength(length);

        readArray(inputStream, tempBuf, 2); // Magic Number

        byte[] shortBuf = new byte[2];
        readArray(inputStream, shortBuf, 2);
        short unknown = byteArrayToShort(shortBuf);
        //checkValidProtocolVersion(protocolVersion);

        readArray(inputStream, tempBuf, 4);
        int operationID = byteArrayToInt(tempBuf);

        readArray(inputStream, tempBuf, 4); // Magic and params
        int bodyLength = length - PACKET_HEADER_SIZE;
        if (bodyLength == 0) return;

        operationID -= 1; // I don't know what this means...

        operation = DanmakuReceivePacket.Operation.forID(operationID);

        body = new byte[bodyLength];
        readArray(inputStream, body, bodyLength);
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

    private void checkValidProtocolVersion(short version) throws BiliLiveException {
        if (version != 1)
            throw new BiliLiveException(I18n.format("msg.danmaku_protocol_error"));
    }

    private void checkValidLength(int length) throws BiliLiveException {
        if (length < 16)
            throw new BiliLiveException(I18n.format("msg.danmaku_protocol_error"));
    }

    /**
     * 指示服务器发回数据类型。
     */
    public enum Operation {
        PLAYER_COUNT(new int[]{0, 1, 2}),
        PLAYER_COMMAND(new int[]{3, 4}),
        UNKNOWN(new int[]{});

        @Getter
        private int[] id;

        Operation(int[] id) {
            this.id = id;
        }

        public static Operation forID(int id) {
            for (Operation operation : Operation.values()){
                for (int findID : operation.id)
                    if(id == findID) return operation;
            }
            return UNKNOWN;
        }
    }
}
