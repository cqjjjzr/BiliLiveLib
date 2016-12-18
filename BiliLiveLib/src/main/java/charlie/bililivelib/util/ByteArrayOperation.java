package charlie.bililivelib.util;

public class ByteArrayOperation {
    public static int byteArrayToInt(byte[] bytes) {
        int value= 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }

    public static short byteArrayToShort(byte[] b) {
        short s0 = (short) b[1];
        short s1 = (short) b[0];
        s1 <<= 8;
        return (short) (s0 | s1);
    }
}
