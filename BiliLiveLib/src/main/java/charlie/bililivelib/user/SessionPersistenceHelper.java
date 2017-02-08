package charlie.bililivelib.user;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Base64;
import java.util.List;

/**
 * 用于将会话对象与Base64字符串互相转换的工具类。该类功能可在{@link Session}类中调用。
 *
 * @author Charlie Jiang
 * @see Session
 * @since rv1
 */
public class SessionPersistenceHelper {
    @Contract(pure = true)
    public static String toBase64(@NotNull @NonNls Session session) {
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
    public static void fromBase64(@NotNull Session session, @NonNls @NotNull String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64.getBytes()));
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
