package charlie.bililivelib.danmaku;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.danmaku.dispatch.DanmakuDispatcher;
import charlie.bililivelib.danmaku.event.DanmakuAdapter;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.datamodel.Room;
import charlie.bililivelib.i18n.I18n;
import charlie.bililivelib.util.LogUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class DanmakuReceiverTest {
    @BeforeClass
    public static void init() {
        I18n.init();
        LogUtil.init();
    }

    public static final int SIXTEEN_ROOMID = 10313;
    @Test
    public void test() throws Exception {
        DanmakuDispatcher.init();
        Room room = new Room();
        room.setRoomID(SIXTEEN_ROOMID);
        DanmakuReceiver receiver = new DanmakuReceiver(room);
        receiver.addDanmakuListener(new TestListener());
        receiver.connect();
        synchronized (this) {
            wait();
        }
    }

    private class TestListener extends DanmakuAdapter {
        private Logger logger = LogManager.getLogger(BiliLiveLib.class);
        @Override
        public void danmakuEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "New Danmaku:" + event.getParam());
        }

        @Override
        public void watcherCountEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "WatcherCount:" + event.getParam());
        }

        @Override
        public void errorEvent(DanmakuEvent event) {
            logger.log(Level.ERROR, event.getParam());
        }

        @Override
        public void statusEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "STATUS:" + event.getParam());
        }
    }
}