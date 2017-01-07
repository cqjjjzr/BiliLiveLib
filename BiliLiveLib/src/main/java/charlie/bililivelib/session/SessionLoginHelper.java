package charlie.bililivelib.session;

import charlie.bililivelib.Globals;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.xml.sax.SAXException;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Set;

import static charlie.bililivelib.session.SessionLoginHelper.LoginStatus.*;

@Data
public class SessionLoginHelper {
    public static final String LOGIN_JAVASCRIPT;
    public static final String TRUST_STORE_PASSWORD = "bilibili";
    public static final int DEFAULT_LOGIN_TIMEOUT_MILLIS = 2000;

    static {
        try {
            LOGIN_JAVASCRIPT = IOUtils.toString(SessionLoginHelper.class.getResourceAsStream("/login.js"),
                    Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new AssertionError("Internal Error: Can't read login.js", e);
        }
    }

    private long loginTimeoutMillis = 2000;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private HtmlPage miniLoginPage;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private WebClient webClient;

    private Session session;
    private String email;
    private String password;

    private LoginStatus status = NOT_COMPLETED;

    public SessionLoginHelper(Session session, String email, String password) {
        this(session, email, password, DEFAULT_LOGIN_TIMEOUT_MILLIS);
    }

    public SessionLoginHelper(Session session, String email, String password, long loginTimeoutMillis) {
        this.session = session;
        this.email = email;
        this.password = password;
        checkArguments();
        this.loginTimeoutMillis = loginTimeoutMillis;

        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        initWebClient();
    }

    private void initWebClient() {
        webClient.getOptions().setSSLTrustStore(SessionLoginHelper.class.getResource("/bili.truststore"),
                TRUST_STORE_PASSWORD,
                "jks");
        avoidUselessErrorMessages();
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new ResynchronizingAjaxController()); // Wait Ajax to eval.
        // We use ResynchronizingAjaxController instead of NicelyResynchronizingAjaxController to avoid logs like:
        // "14:20:09.451 [main] INFO  c.g.h.NicelyResynchronizingAjaxController
        // - Re-synchronized call to https://passport.bilibili.com/ajax/miniLogin/login"

        webClient.setCache(Globals.get().getHtmlUnitCache());
    }

    private void checkArguments() {
        if (session == null ||
                email == null ||
                email.isEmpty() ||
                password == null ||
                password.isEmpty())
            throw new IllegalArgumentException("Email=" + Objects.toString(email, "null") + "\n" +
                    "Password=" + Objects.toString(password, "null") + "\n" +
                    "Session=" + Objects.toString(session, "null"));
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

    public void startLogin() throws IOException {
        status = NOT_COMPLETED;
        miniLoginPage = webClient.getPage("https://passport.bilibili.com/ajax/miniLogin/minilogin");
        miniLoginPage.executeJavaScript(LOGIN_JAVASCRIPT);
        webClient.setStatusHandler((page, message) -> {
            if (page == miniLoginPage) {
                parseStatus(message);
            }
        });
    }

    private void parseStatus(String message) {
        if (message.startsWith("_keyerr")) {
            status = KEY_ERROR;
            return;
        }
        Gson gson = Globals.get().getGson();
        JsonObject rootObject = gson.fromJson(message, JsonElement.class).getAsJsonObject();
        if (rootObject.get("status").getAsBoolean()) {
            status = SUCCESS;
            return;
        }

        int errorCode = rootObject.get("message").getAsJsonObject()
                .get("code").getAsInt();
        status = LoginStatus.forCode(errorCode);
    }

    public Image getCaptcha() throws IOException {
        HtmlImage image = (HtmlImage) miniLoginPage.getByXPath("//img[@class='captcha-img']").get(0);
        return image.getImageReader().read(0);
    }

    public void loginWithCaptcha(String captcha) throws IOException, SAXException {
        miniLoginPage.getElementById("login-username").setAttribute("value", email);
        miniLoginPage.getElementById("login-passwd").setAttribute("value", password);
        miniLoginPage.getElementById("login-captcha").setAttribute("value", captcha);

        // This operation will block to wait until ajax performs completely.
        // Thanks to NicelyResynchronizingAjaxController.
        miniLoginPage.getHtmlElementById("login-submit").click();

        // WARNING: USED Experimental API HERE!
        // webClient.waitForBackgroundJavaScript(loginTimeoutMillis);
        // Needn't this if we use NicelyResynchronizingAjaxController. See SessionLoginHelper().
    }

    public LoginStatus getLoginStatus() {
        return status;
    }

    private boolean isValidMessageElement(DomElement domElement) {
        return domElement.getAttribute("class").equals("message")
                && !domElement.getTextContent().trim().isEmpty();
    }

    private boolean checkSuccess() {
        //bilibili will open https://passport.bilibili.com/ajax/miniLogin/redirect if login success.
        return webClient.getCurrentWindow().getEnclosedPage().getUrl().toString().contains("redirect");
    }

    public void fillSession() {
        if (status != SUCCESS) throw new IllegalStateException("Bad status: " + status);
        Set<Cookie> cookies = webClient.getCookieManager().getCookies();

        CookieStore store = session.getCookieStore();
        store.clear();
        for (Cookie cookie : cookies) {
            store.addCookie(cookie.toHttpClient()); // HtmlUnit Cookie to HttpClient Cookie
        }
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
