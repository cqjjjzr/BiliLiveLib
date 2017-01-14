package charlie.bililivelib;

import charlie.bililivelib.danmaku.DanmakuReceiver;
import charlie.bililivelib.danmaku.dispatch.GlobalAnnounceDispatcher;
import charlie.bililivelib.danmaku.dispatch.GlobalGiftDispatcher;
import charlie.bililivelib.danmaku.event.DanmakuAdapter;
import charlie.bililivelib.danmaku.event.DanmakuEvent;
import charlie.bililivelib.room.Room;
import charlie.bililivelib.session.Session;
import charlie.bililivelib.session.SessionLoginHelper;
import charlie.bililivelib.smalltv.SmallTV;
import charlie.bililivelib.smalltv.SmallTVProtocol;
import charlie.bililivelib.smalltv.SmallTVReward;
import charlie.bililivelib.util.I18n;
import charlie.bililivelib.util.LogUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.assertTrue;

public class DanmakuReceiverAndSmallTVTest {
    public static final int SIXTEEN_ROOMID = 10313;
    public static final int CHARLIE_JIANG_ROOMID = 39249;
    public static final int MARCOV_ROOMID = 17029;
    private static Session session;

    @BeforeClass
    public static void init() throws IOException {
        I18n.init();
        LogUtil.init();
        session = new Session(Globals.get().getConnectionPool());

        initSession();
    }

    private static void initSession() throws IOException {
        Path cookieFile = Paths.get("cookies.bin");
        if (Files.exists(cookieFile)) {
            loadSessionFromFile();
        } else {
            login();
            Files.createFile(cookieFile);
        }
        session.activate();

        Files.write(cookieFile, session.toBase64().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void login() throws IOException {
        String email = testInput("E-Mail:");
        String password = testInput("Password:");

        SessionLoginHelper helper = new SessionLoginHelper(email, password,
                SessionLoginHelper.DEFAULT_LOGIN_TIMEOUT_MILLIS,
                true);
        helper.startLogin();
        JOptionPane.showMessageDialog(null, "Captcha", "Captcha",
                JOptionPane.PLAIN_MESSAGE, new ImageIcon(helper.getCaptcha()));

        String captcha = testInput("Captcha:");
        helper.loginWithCaptcha(captcha);

        System.out.println(helper.getStatus());
        assertTrue(helper.getStatus() == SessionLoginHelper.LoginStatus.SUCCESS);

        helper.fillSession(session);
    }

    private static void loadSessionFromFile() throws IOException {
        String xml = new String(Files.readAllBytes(Paths.get("cookies.bin")));
        session.fromBase64(xml);
    }

    private static String testInput(String message) {
        return JOptionPane.showInputDialog(null, message, "Test",
                JOptionPane.PLAIN_MESSAGE);
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
        public void welcomeVipEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "Welcome VIP:" + event.getParam());
        }

        @Override
        public void danmakuEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "New Danmaku:" + event.getParam());
        }

        @Override
        public void watcherCountEvent(DanmakuEvent event) {
            // logger.log(Level.INFO, "WatcherCount:" + event.getParam());
        }

        @Override
        public void startStopEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "StartStop:" + event.getParam());
        }

        @Override
        public void errorEvent(DanmakuEvent event) {
            logger.log(Level.ERROR, event.getParam());
            ((Throwable) event.getParam()).printStackTrace();
        }

        @Override
        public void statusEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "STATUS:" + event.getParam());
        }

        @Override
        public void giveGiftEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "Give gift:" + event.getParam());
        }

        @Override
        public void globalGiftEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "Global SmallTV:" + event.getParam());
            try {
                smallTVProtocol.joinLottery((SmallTV) event.getParam());
                logger.log(Level.INFO, "Joined Global SmallTV:" + event.getParam());
            } catch (BiliLiveException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void globalAnnounceEvent(DanmakuEvent event) {
            logger.log(Level.INFO, "GlobalAnnounce:" + event.getParam());
        }
    }
}