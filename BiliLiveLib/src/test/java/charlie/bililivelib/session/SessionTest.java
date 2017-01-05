package charlie.bililivelib.session;

import charlie.bililivelib.util.I18n;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.beans.Introspector;
import java.util.Arrays;
import java.util.Calendar;

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
    public void loginWithCaptcha() throws Exception {
        // TODO Session Login Helper!
    }

    private String testInput(String message) {
        return JOptionPane.showInputDialog(null, message, "Test",
                JOptionPane.PLAIN_MESSAGE);
    }
}