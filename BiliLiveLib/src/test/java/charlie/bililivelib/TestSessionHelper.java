package charlie.bililivelib;

import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.user.Session;
import charlie.bililivelib.user.SessionLoginHelper;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.assertTrue;

public class TestSessionHelper {
    public static Session initSession() throws IOException, BiliLiveException {
        Session session = new Session(Globals.get().getConnectionPool());
        Path cookieFile = Paths.get("cookies.bin");
        if (Files.exists(cookieFile)) {
            loadSessionFromFile(session);
        } else {
            login(session);
            Files.createFile(cookieFile);
        }

        Files.write(cookieFile, session.toBase64().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        return session;
    }

    private static void login(Session session) throws BiliLiveException {
        String email = testInput("E-Mail:");
        String password = testInput("Password:");

        SessionLoginHelper helper = new SessionLoginHelper(email, password,
                SessionLoginHelper.DEFAULT_LOGIN_TIMEOUT_MILLIS,
                true);
        JOptionPane.showMessageDialog(null, "Captcha", "Captcha",
                JOptionPane.PLAIN_MESSAGE, new ImageIcon(helper.getCaptcha()));

        String captcha = testInput("Captcha:");
        helper.loginWithCaptcha(captcha);

        System.out.println(helper.getStatus());
        assertTrue(helper.getStatus() == SessionLoginHelper.LoginStatus.SUCCESS);

        helper.fillSession(session);
    }

    private static void loadSessionFromFile(Session session) throws IOException {
        String xml = new String(Files.readAllBytes(Paths.get("cookies.bin")));
        session.fromBase64(xml);
    }

    static String testInput(String message) {
        return JOptionPane.showInputDialog(null, message, "Test",
                JOptionPane.PLAIN_MESSAGE);
    }

}
