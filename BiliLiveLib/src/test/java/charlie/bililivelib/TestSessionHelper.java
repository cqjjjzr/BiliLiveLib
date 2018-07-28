package charlie.bililivelib;

import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.user.Session;
import charlie.bililivelib.user.SessionLoginHelper;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.assertTrue;

public class TestSessionHelper {
    public static Session initSession() throws IOException, BiliLiveException {
        BasicCookieStore store = new BasicCookieStore();
        Session session = new Session(HttpClientBuilder.create()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36")
                .setConnectionManager(Globals.get().getConnectionPool())
                .setSSLContext(Globals.get().getBilibiliSSLContext())
                .setDefaultCookieStore(store).build(), store);
        Path cookieFile = Paths.get("cookies.bin");
        if (Files.exists(cookieFile)) {
            loadSessionFromFile(session);
        } else {
            login(session);
            Files.createFile(cookieFile);
        }

        ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(cookieFile, StandardOpenOption.TRUNCATE_EXISTING));
        stream.writeObject(store);
        stream.close();
        return session;
    }

    private static void login(Session session) throws BiliLiveException {
        String email = testInput("E-Mail:");
        String password = testInput("Password:");

        SessionLoginHelper helper = new SessionLoginHelper(email, password,
                SessionLoginHelper.DEFAULT_LOGIN_TIMEOUT_MILLIS,
                true, BiliLiveLib.DEFAULT_USER_AGENT);
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

    public static void main(String[] args) throws IOException, BiliLiveException {
        initSession();
    }
}
