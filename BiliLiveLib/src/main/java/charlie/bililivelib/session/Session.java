package charlie.bililivelib.session;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.Globals;
import charlie.bililivelib.net.HttpHelper;
import lombok.Getter;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.beans.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

@Getter
public class Session {
    private static final String XML_TAIL = "<!-- DON'T MODIFY THIS FILE! IT'S MACHINE GENERATED. -->";
    public static final CookiePersistenceDelegate COOKIE_PERSISTENCE_DELEGATE = new CookiePersistenceDelegate();

    private HttpHelper httpHelper;
    private CookieStore cookieStore;

    public Session() {
        this(Globals.get().getConnectionPool());
    }

    public Session(HttpClient httpClient, CookieStore cookieStore) {
        httpHelper = new HttpHelper();
        httpHelper.init(httpClient);
        this.cookieStore = cookieStore;
    }

    public Session(HttpClientConnectionManager clientConnectionManager) {
        httpHelper = new HttpHelper();
        initHttpHelper(clientConnectionManager);
    }

    private void initHttpHelper(HttpClientConnectionManager clientConnectionManager) {
        cookieStore = new BasicCookieStore();
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setUserAgent("BiliLiveLib " + BiliLiveLib.VERSION)
                .setConnectionManager(clientConnectionManager)
                .setDefaultCookieStore(cookieStore);
        httpHelper.init(builder.build());
    }

    public String toXML() {
        try {
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(PersistenceCookie.class)
                    .getPropertyDescriptors();

            for (PropertyDescriptor descriptor : descriptors) {
                if (descriptor.getName().equals("commentURL") || descriptor.getName().equals("ports"))
                    descriptor.setValue("transient", true);
            }
        } catch (IntrospectionException e) {
            return null;
        }

        List<Cookie> cookies = cookieStore.getCookies();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(outputStream);
        encoder.setPersistenceDelegate(Cookie.class, COOKIE_PERSISTENCE_DELEGATE);

        encoder.writeObject(cookies.size());
        for (Cookie cookie : cookies) {
            encoder.writeObject(cookie instanceof PersistenceCookie ? cookie : new PersistenceCookie(cookie));
        }
        encoder.close();

        return new String(outputStream.toByteArray()) + XML_TAIL;
    }

    public void fromXML(String xml) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        XMLDecoder decoder = new XMLDecoder(inputStream);

        int size = (Integer) decoder.readObject();

        cookieStore.clear();

        for(int i = 0;i < size;i++) {
            cookieStore.addCookie((Cookie) decoder.readObject());
        }
        decoder.close();
    }

    public static Cookie createCookie(
            Object name,
            Object value,
            Object domain,
            Object comment,
            Object expiryDate,
            Object path,
            Object secure,
            Object version) {
        BasicClientCookie cookie = new BasicClientCookie((String) name, (String) value);

        cookie.setDomain((String) domain);
        cookie.setExpiryDate(expiryDate == null ? null : new Date((Long) expiryDate));
        cookie.setComment((String) comment);
        cookie.setPath((String) path);
        cookie.setSecure((Boolean) secure);
        cookie.setVersion((Integer) version);
        return cookie;
    }

    private static class CookiePersistenceDelegate extends DefaultPersistenceDelegate {
        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Cookie cookie = (Cookie) oldInstance;
            return new Expression(oldInstance, PersistenceCookie.class, "new", new Object[]{
                    cookie.getName(),
                    cookie.getValue(),
                    cookie.getDomain(),
                    cookie.getComment(),
                    (cookie.getExpiryDate() == null) ? null : cookie.getExpiryDate().getTime(),
                    cookie.getPath(),
                    cookie.isSecure(),
                    cookie.getVersion()
            });
        }
    }

    public static class PersistenceCookie extends BasicClientCookie {
        /**
         * Default Constructor taking a name and a value. The value may be null.
         *
         * @param name  The name.
         * @param value The value.
         */
        public PersistenceCookie(String name, String value) {
            super(name, value);
        }

        public PersistenceCookie(Cookie cookie) {
            this(cookie.getName(), cookie.getValue());

            setDomain(cookie.getDomain());
            setExpiryDate(cookie.getExpiryDate());
            setComment(cookie.getComment());
            setPath(cookie.getPath());
            setSecure(cookie.isSecure());
            setVersion(cookie.getVersion());
        }

        public PersistenceCookie(
                String name,
                String value,
                String domain,
                String comment,
                Long expiryDateMillis,
                String path,
                Boolean secure,
                Integer version) {
            this(name, value);

            setDomain(domain);
            setExpiryDate(expiryDateMillis == null ? null : new Date(expiryDateMillis));
            setComment(comment);
            setPath(path);
            setSecure(secure);
            setVersion(version);
        }

        @ConstructorProperties({"name", "value", "domain", "comment", "expiryDate", "path", "secure", "version"})
        public PersistenceCookie(
                Object name,
                Object value,
                Object domain,
                Object comment,
                Object expiryDate,
                Object path,
                Object secure,
                Object version) {
            this((String) name, (String) value);

            setDomain((String) domain);
            setExpiryDate((Date) expiryDate);
            setComment((String) comment);
            setPath((String) path);
            setSecure((Boolean) secure);
            setVersion((Integer) version);
        }
    }
}
