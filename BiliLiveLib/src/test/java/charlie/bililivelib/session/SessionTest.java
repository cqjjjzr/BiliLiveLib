package charlie.bililivelib.session;

import charlie.bililivelib.Globals;
import charlie.bililivelib.util.I18n;
import org.apache.http.HttpHost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.beans.Introspector;
import java.util.Arrays;
import java.util.Calendar;

import static org.junit.Assert.assertTrue;

public class SessionTest {
    @BeforeClass
    public static void setUp() throws Exception {
        I18n.init();
    }

    @Ignore
    public void showIntrospection() throws Exception {
        System.out.println(Arrays.toString(Introspector.getBeanInfo(
                Cookie.class).getPropertyDescriptors()));
    }

    @Test
    public void persistenceTest() throws Exception {
        BasicCookieStore store = new BasicCookieStore();
        Session session = new Session(null, store);
        BasicClientCookie cookie = new BasicClientCookie("name1", "value1");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2200, 5, 3, 22, 33, 55);
        cookie.setExpiryDate(calendar.getTime());
        cookie.setDomain("live.bilibili.com");
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setComment("LIVE.");
        cookie.setVersion(1);
        store.addCookie(cookie);
        String xml = session.toXML();
        System.out.println(xml);

        store = new BasicCookieStore();
        Session newSession = new Session(null, store);
        newSession.fromXML(xml);
        System.out.println(store);
    }

    @Test
    public void persistenceTestWithNullFields() throws Exception {
        BasicCookieStore store = new BasicCookieStore();
        Session session = new Session(null, store);
        BasicClientCookie cookie = new BasicClientCookie("name1", "value1");
        store.addCookie(cookie);
        String xml = session.toXML();
        System.out.println(xml);

        store = new BasicCookieStore();
        session = new Session(null, store);
        session.fromXML(xml);
        System.out.println(store);
    }

    @Test
    public void loginWithoutCaptcha() throws Exception {
        Session session = new Session();
        String email = JOptionPane.showInputDialog(null, "E-Mail:", "Test",
                JOptionPane.PLAIN_MESSAGE);
        String password = JOptionPane.showInputDialog(null, "Password:", "Test",
                JOptionPane.PLAIN_MESSAGE);

        Session.LoginResult result = session.login(email, password);
        System.out.println(result.getStatus());
        assertTrue(!result.isSuccess());
    }

    @Test
    public void loginWithCaptcha() throws Exception {
        BasicCookieStore store = new BasicCookieStore();
        Session session = new Session(HttpClientBuilder.create()
                .setProxy(new HttpHost("127.0.0.1", 8888))
                .setConnectionManager(Globals.get().getConnectionPool())
                .setSSLContext(Globals.get().getBilibiliSSLContext())
                .setDefaultCookieStore(store)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36")
                .build(), store);

        String email = testInput("E-Mail:");
        String password = testInput("Password:");
        Image captchaImage = session.getCaptcha();
        JOptionPane.showMessageDialog(null, "Captcha Image:", "Test",
                JOptionPane.PLAIN_MESSAGE, new ImageIcon(captchaImage));
        String captcha = testInput("Captcha:");

        Session.LoginResult result = session.login(email, password, captcha);
        System.out.println(result.getStatus());
        assertTrue(result.isSuccess());
    }

    private String testInput(String message) {
        return JOptionPane.showInputDialog(null, message, "Test",
                JOptionPane.PLAIN_MESSAGE);
    }
}