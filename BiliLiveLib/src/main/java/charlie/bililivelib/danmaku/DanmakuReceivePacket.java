package charlie.bililivelib.danmaku;

import charlie.bililivelib.I18n;
import charlie.bililivelib.exceptions.BiliLiveException;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;

import static charlie.bililivelib.internalutil.ByteArrayOperation.byteArrayToInt;
import static charlie.bililivelib.internalutil.ByteArrayOperation.byteArrayToShort;

@Getter
public final class DanmakuReceivePacket {
    private static final int PACKET_HEADER_SIZE = 16;

    private Operation operation = null;
    private byte[] body = null;

    public DanmakuReceivePacket(InputStream inputStream) throws IOException, BiliLiveException {
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
        int bodyLength = length - PACKET_HEADER_SIZE;
        if (bodyLength == 0) return;

        operationID -= 1; // I don't know what this means...

        DanmakuReceivePacket.Operation operation = DanmakuReceivePacket.Operation.forID(operationID);

        byte[] bodyBuffer = new byte[bodyLength];
        readArray(inputStream, bodyBuffer, bodyLength);
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
