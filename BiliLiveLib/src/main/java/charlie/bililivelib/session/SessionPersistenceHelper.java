package charlie.bililivelib.session;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.beans.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

public class SessionPersistenceHelper {
    private static final PersistenceDelegate COOKIE_PERSISTENCE_DELEGATE = new DefaultPersistenceDelegate(
            new String[]{"name", "value", "domain", "comment", "expiryDate", "path", "secure", "version"}
    );
    private static final String XML_TAIL = "<!-- DON'T MODIFY THIS FILE! IT'S MACHINE GENERATED. -->";


    static {
        try {
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(PersistenceCookie.class)
                    .getPropertyDescriptors();

            for (PropertyDescriptor descriptor : descriptors) {
                if (descriptor.getName().equals("commentURL") || descriptor.getName().equals("ports"))
                    descriptor.setValue("transient", true);
            }
        } catch (IntrospectionException ignored) {
        }
    }

    @Contract(pure = true)
    public static String toXML(@NotNull Session session) {
        List<Cookie> cookies = session.getCookieStore().getCookies();

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

    public static void fromXML(@NotNull Session session, @NonNls String xml) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        XMLDecoder decoder = new XMLDecoder(inputStream);

        int size = (Integer) decoder.readObject();

        session.getCookieStore().clear();

        for (int i = 0; i < size; i++) {
            session.getCookieStore().addCookie((Cookie) decoder.readObject());
        }
        decoder.close();
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
            setExpiryDate(expiryDate == null ? null : (Date) expiryDate);
            setComment((String) comment);
            setPath((String) path);
            setSecure((Boolean) secure);
            setVersion((Integer) version);
        }
    }
}
