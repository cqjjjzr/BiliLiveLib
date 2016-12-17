package charlie.bililivelib.danmaku;

import org.junit.Test;

import static org.junit.Assert.*;

public class DanmakuPacketTest {
    @Test
    public void generate() throws Exception {
        DanmakuPacket packet = new DanmakuPacket(DanmakuPacket.Action.HEARTBEAT, "sss");
        byte[] buffer = packet.generate();
        assertArrayEquals(new byte[]{0, 0, 0, 19, 0, 16, 0, 1, 0, 0, 0, 2, 0, 0, 0, 1, 115, 115, 115}, buffer);
    }
}