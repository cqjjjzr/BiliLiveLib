package charlie.bililivelib.user;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.Globals;
import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.exceptions.NetworkException;
import charlie.bililivelib.exceptions.WrongCaptchaException;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Set;

import static charlie.bililivelib.user.SessionLoginHelper.LoginStatus.*;

/**
 * 用于登录会话的工具类。注意，由于无法直接访问Bilibili直播登录API，因此本类直接使用了HtmlUnit进行模拟登录。<br />
 * 因此，该类的执行效率极低，请勿频繁使用此类。<br />
 * <red>约定：本类所有public方法在传入Null参数时抛出NullPointerException。</red>
 *
 * @author Charlie Jiang
 * @see Session
 * @since rv1
 */
@Getter
public class SessionLoginHelper {
    public static final int DEFAULT_LOGIN_TIMEOUT_MILLIS = 2000;
    public static final boolean DEFAULT_KEEP_LOGGING_IN = true;

    public static final String LOGIN_JAVASCRIPT;
    public static final String TRUST_STORE_PASSWORD = "bilibili";
    public static final String MINILOGIN_URL = "https://passport.bilibili.com/ajax/miniLogin/minilogin";

    static {
        try {
            LOGIN_JAVASCRIPT = IOUtils.toString(SessionLoginHelper.class.getResourceAsStream("/login.js"),
                    Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new AssertionError("Internal Error: Can't read login.js", e);
        }
    }

    private long loginTimeoutMillis;
    private HtmlPage miniLoginPage;
    private WebClient webClient;

    private String email;
    private String password;
    private boolean keepLoggingIn;

    private LoginStatus status = NOT_COMPLETED;

    /**
     * 对于给定登录凭据，使用默认User-Agent和默认登录超时开始登录会话，且保持登录。
     * @param email E-Mail/ID/手机号
     * @param password 明文密码
     * @throws NetworkException 在出现网络问题时抛出
     * @throws IllegalArgumentException 在参数无效时抛出
     * @throws NullPointerException 在有参数为Null时抛出
     */
    public SessionLoginHelper(@NotNull String email, @NotNull String password) throws BiliLiveException {
        this(email, password, BiliLiveLib.DEFAULT_USER_AGENT);
    }

    /**
     * 对于给定登录凭据，使用给定User-Agent和默认登录超时开始登录会话，且保持登录。
     *
     * @param email     E-Mail/ID/手机号
     * @param password  明文密码
     * @param userAgent 给定User-Agent
     * @throws NetworkException         在出现网络问题时抛出
     * @throws IllegalArgumentException 在参数无效时抛出
     * @throws NullPointerException     在有参数为Null时抛出
     */
    public SessionLoginHelper(@NotNull String email, @NotNull String password, @NotNull String userAgent)
            throws BiliLiveException {
        this(email, password, DEFAULT_LOGIN_TIMEOUT_MILLIS, DEFAULT_KEEP_LOGGING_IN, userAgent);
    }

    /**
     * 对于给定登录凭据，使用给定User-Agent和给定登录超时开始登录会话，且指定是否保持登录。
     * @param email E-Mail/ID/手机号
     * @param password 明文密码
     * @param userAgent User-Agent
     * @param keepLoggingIn 指定是否保持登录
     * @param loginTimeoutMillis 登录执行超时
     * @throws NetworkException 在出现网络问题时抛出
     * @throws IllegalArgumentException 在参数无效时抛出
     * @throws NullPointerException 在有参数为Null时抛出
     */
    public SessionLoginHelper(@NotNull String email, @NotNull String password,
                              long loginTimeoutMillis, boolean keepLoggingIn, @NotNull String userAgent)
            throws BiliLiveException {
        this.email = email;
        this.password = password;
        this.loginTimeoutMillis = loginTimeoutMillis;
        this.keepLoggingIn = keepLoggingIn;
        checkArguments(userAgent);

        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED/*, "127.0.0.1", 8888*/); // Commented code is for Fiddler Debugging.
        initWebClient(userAgent);
        startLogin();
    }

    private void initWebClient(String userAgent) {
        /*webClient.getOptions().setSSLTrustStore(SessionLoginHelper.class.getResource("/bili.truststore"),
                TRUST_STORE_PASSWORD,
                "jks");*/
        avoidUselessErrorMessages();
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new ResynchronizingAjaxController()); // Wait Ajax to eval.
        // We use ResynchronizingAjaxController instead of NicelyResynchronizingAjaxController to avoid logs like:
        // "14:20:09.451 [main] INFO  c.g.h.NicelyResynchronizingAjaxController
        // - Re-synchronized call to https://passport.bilibili.com/ajax/miniLogin/login"

        webClient.setCache(Globals.get().getHtmlUnitCache());
        webClient.addRequestHeader("User-Agent", userAgent);
    }

    private void checkArguments(String userAgent) {
        if (email == null || password == null || userAgent == null)
            throw new NullPointerException();
        if (email.isEmpty() || password.isEmpty())
            throw new IllegalArgumentException("Email=" + Objects.toString(email, "null") + "\n" +
                    "Password=" + Objects.toString(password, "null"));
        if (loginTimeoutMillis < 1)
            throw new IllegalArgumentException("loginTimeoutMillis < 1");
    }

    private void avoidUselessErrorMessages() {
        webClient.setIncorrectnessListener(new IncorrectnessListenerImpl() {
            @Override
            public void notify(String message, Object origin) {
                if (!message.contains("application/javascript"))
                    super.notify(message, origin);
                //avoid error messages like:
                // "[main] WARN  c.g.h.IncorrectnessListenerImpl -
                // Expected content type of 'application/javascript' or 'application/ecmascript'
                // for remotely loaded JavaScript element
                // at 'https://data.bilibili.com/rec.js', but got 'text/html'."
            }
        });
    }

    private void startLogin() throws BiliLiveException {
        try {
            status = NOT_COMPLETED;
            miniLoginPage = webClient.getPage(MINILOGIN_URL);
            miniLoginPage.executeJavaScript(LOGIN_JAVASCRIPT);
            webClient.setStatusHandler((page, message) -> {
                if (page == miniLoginPage) {
                    parseStatus(message);
                }
            });
        } catch (IOException ex) {
            throw new NetworkException("IO Exception", ex);
        }
    }

    private void parseStatus(@NonNls String statusJSON) {
        // See login.js!
        // In login.js we use _keyerr to label error occurred in getting RSA public key.
        if (statusJSON.startsWith("_keyerr")) {
            status = KEY_ERROR;
            return;
        }
        Gson gson = Globals.get().gson();
        JsonObject rootObject = gson.fromJson(statusJSON, JsonElement.class).getAsJsonObject();
        if (rootObject.get("status").getAsBoolean()) {
            status = SUCCESS;
            return;
        }

        int errorCode = rootObject.get("message").getAsJsonObject()
                .get("code").getAsInt();
        status = LoginStatus.forCode(errorCode);
    }

    /**
     * 刷新并获取验证码。
     * @return 验证码
     * @throws NetworkException 在发生网络问题时抛出
     */
    public Image getCaptcha() throws BiliLiveException {
        try {
            HtmlImage image = (HtmlImage) miniLoginPage.getByXPath("//img[@class='captcha-img']").get(0);
            image.click(); // Click to refresh.
            return image.getImageReader().read(0);
        } catch (IOException ex) {
            throw new NetworkException("IO Exception", ex);
        }
    }

    /**
     * 使用给定验证码进行登录。登录结果保存在本类中。
     * @param captcha 验证码
     * @throws NetworkException 在发生网络问题时抛出
     * @throws WrongCaptchaException 在验证码错误时抛出
     */
    public void loginWithCaptcha(String captcha) throws BiliLiveException {
        try {
            miniLoginPage.getElementById("login-username").setAttribute("value", email);
            miniLoginPage.getElementById("login-passwd").setAttribute("value", password);
            miniLoginPage.getElementById("login-captcha").setAttribute("value", captcha);

            // This operation will block to wait until ajax performs completely.
            // Thanks to NicelyResynchronizingAjaxController.
            miniLoginPage.getHtmlElementById("login-submit").click();
            if (status == WRONG_CAPTCHA) throw new WrongCaptchaException();

            // WARNING: USED Experimental API HERE!
            // webClient.waitForBackgroundJavaScript(loginTimeoutMillis);
            // Needn't this if we use NicelyResynchronizingAjaxController. See SessionLoginHelper().
        } catch (IOException ex) {
            throw new NetworkException("IO Exception", ex);
        }
    }

    /**
     * 将登录结果保存到给定会话对象中，并激活给定会话对象。
     * @param session 会话对象
     * @throws IllegalStateException 登录未成功时抛出
     * @throws NetworkException 在发生网络问题时抛出
     */
    public void fillSession(Session session) throws BiliLiveException {
        try {
            if (status != SUCCESS) throw new IllegalStateException("Bad status: " + status);

            Set<Cookie> cookies = webClient.getCookieManager().getCookies();

            CookieStore store = session.getCookieStore();
            store.clear();
            for (Cookie cookie : cookies) {
                store.addCookie(cookie.toHttpClient()); // HtmlUnit Cookie to HttpClient Cookie
            }

            if (getStatus() == SUCCESS) session.activate();
        } catch (IOException ex) {
            throw new NetworkException("IO Exception", ex);
        }
    }

    public void setLoginTimeoutMillis(long loginTimeoutMillis) {
        if (loginTimeoutMillis < 1) throw new IllegalArgumentException("Login Timeout < 1ms");
        this.loginTimeoutMillis = loginTimeoutMillis;
    }

    public void setEmail(@NotNull String email) {
        if (email.isEmpty()) throw new IllegalArgumentException("E-Mail is empty");
        this.email = email;
    }

    public void setPassword(@NotNull String password) {
        if (password.isEmpty()) throw new IllegalArgumentException("Password is empty");
        this.password = password;
    }

    public enum LoginStatus {
        NOT_COMPLETED(2), SUCCESS(0), UNKNOWN(1), KEY_ERROR(3), // NOT DEFINED IN ERROR MAP
        WRONG_CAPTCHA(-105), USER_NOT_EXISTS(-626), WRONG_PASSWORD(-627), TOO_MANY_RETRIES(-625),
        PASSWORD_EXPIRED(-662) // DEFINED IN ERROR MAP IN minilogin.js
        ;
        @Getter
        private int id;

        LoginStatus(int id) {
            this.id = id;
        }

        public static LoginStatus forCode(int errorCode) {
            for (LoginStatus status : LoginStatus.values()) {
                if (status.id == errorCode) return status;
            }
            return UNKNOWN;
        }
    }
}
