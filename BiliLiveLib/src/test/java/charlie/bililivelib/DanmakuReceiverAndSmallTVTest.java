package charlie.bililivelib;

import charlie.bililivelib.danmaku.DanmakuReceiver;
import charlie.bililivelib.danmaku.dispatch.GlobalAnnounceDispatcher;
import charlie.bililivelib.danmaku.dispatch.GlobalGiftDispatcher;
import charlie.bililivelib.danmaku.event.DanmakuAdapter;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.room.Room;
import charlie.bililivelib.smalltv.SmallTV;
import charlie.bililivelib.smalltv.SmallTVProtocol;
import charlie.bililivelib.smalltv.SmallTVReward;
import charlie.bililivelib.user.Session;
import charlie.bililivelib.util.I18n;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static charlie.bililivelib.TestSessionHelper.initSession;
import static charlie.bililivelib.TestSessionHelper.testInput;
import static org.junit.Assert.assertTrue;

public class DanmakuReceiverAndSmallTVTest {
    public static final int SIXTEEN_ROOMID = 10313;
    public static final int CHARLIE_JIANG_ROOMID = 39249;
    public static final int MARCOV_ROOMID = 17029;
    private static Session session;

    @BeforeClass
    public static void init() throws Exception {
        I18n.init();
        LogUtil.init();

        session = initSession();
    }

    @Ignore
    public void testSmallTV() throws Exception {
        int smallTV = Integer.parseInt(testInput("Input a SmallTV ID:"));

        SmallTVReward reward = new SmallTVProtocol(session).getReward(smallTV);

        assertTrue(!reward.isMiss());
        assert reward.getReward() != null;
        System.out.println(reward.getReward().getKind().getDisplayName() + " x " + reward.getReward().getCount());
    }

    @Test
    public void testDanmaku() throws Exception {
        Room room = new Room(SIXTEEN_ROOMID, session);
        DanmakuReceiver receiver = new DanmakuReceiver(room,
                DanmakuReceiver.generateRandomUID(), DanmakuReceiver.CMT_SERVERS[1]);
        /*receiver.getDispatchManager().registerDispatcher(new DanmakuDispatcher());
        receiver.getDispatchManager().registerDispatcher(new StartStopDispatcher());
        receiver.getDispatchManager().registerDispatcher(new WelcomeVipDispatcher());
        receiver.getDispatchManager().registerDispatcher(new GiveGiftDispatcher());*/
        receiver.getDispatchManager().registerDispatcher(new GlobalGiftDispatcher());
        receiver.getDispatchManager().registerDispatcher(new GlobalAnnounceDispatcher());
        receiver.addDanmakuListener(new TestListener());
        receiver.connect();
        synchronized (this) {
            wait();
        }
    }

    private class TestListener extends DanmakuAdapter {
        private Logger logger = LogManager.getLogger(BiliLiveLib.class);
        private SmallTVProtocol smallTVProtocol = new SmallTVProtocol(session);

        @Override
        public void welcomeVipEvent(@NotNull DanmakuEvent event) {
            logger.log(Level.INFO, "Welcome VIP:" + event.getParam());
        }

        @Override
        public void danmakuEvent(@NotNull DanmakuEvent event) {
            logger.log(Level.INFO, "New Danmaku:" + event.getParam());
        }

        @Override
        public void watcherCountEvent(@NotNull DanmakuEvent event) {
            // logger.log(Level.INFO, "WatcherCount:" + event.getParam());
        }

        @Override
        public void startStopEvent(@NotNull DanmakuEvent event) {
            logger.log(Level.INFO, "StartStop:" + event.getParam());
        }

        @Override
        public void errorEvent(@NotNull DanmakuEvent event) {
            logger.log(Level.ERROR, event.getParam());
            ((Throwable) event.getParam()).printStackTrace();
        }

        @Override
        public void statusEvent(@NotNull DanmakuEvent event) {
            logger.log(Level.INFO, "STATUS:" + event.getParam());
        }

        @Override
        public void giveGiftEvent(@NotNull DanmakuEvent event) {
            logger.log(Level.INFO, "Give gift:" + event.getParam());
        }

        @Override
        public void globalGiftEvent(@NotNull DanmakuEvent event) {
            logger.log(Level.INFO, "Global SmallTV:" + event.getParam());
            try {
                smallTVProtocol.joinLottery((SmallTV) event.getParam());
                logger.log(Level.INFO, "Joined Global SmallTV:" + event.getParam());
            } catch (BiliLiveException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void globalAnnounceEvent(@NotNull DanmakuEvent event) {
            logger.log(Level.INFO, "GlobalAnnounce:" + event.getParam());
        }
    }
}