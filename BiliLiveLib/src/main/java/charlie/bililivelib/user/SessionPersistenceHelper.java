package charlie.bililivelib.user;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Base64;
import java.util.List;

public class SessionPersistenceHelper {
    @Contract(pure = true)
    public static String toBase64(@NotNull Session session) {
        List<Cookie> cookies = session.getCookieStore().getCookies();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(cookies);
            objectOutputStream.close();
            return new String(Base64.getEncoder().encode(outputStream.toByteArray()));
        } catch (IOException e) {
            throw new AssertionError();
        }
    }

    @SuppressWarnings("unchecked")
    public static void fromBase64(@NotNull Session session, @NonNls String xml) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(xml.getBytes()));
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            List<Cookie> cookies = (List<Cookie>) objectInputStream.readObject();

            CookieStore store = session.getCookieStore();
            store.clear();
            for (Cookie cookie : cookies) {
                store.addCookie(cookie);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError();
        }
    }
}
