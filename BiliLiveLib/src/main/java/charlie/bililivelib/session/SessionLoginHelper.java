package charlie.bililivelib.session;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.xml.sax.SAXException;

import java.awt.*;
import java.io.IOException;

import static charlie.bililivelib.session.SessionLoginHelper.LoginStatus.*;

@Data
public class SessionLoginHelper {
    public static final int DEFALUE_LOGIN_TIMEOUT_MILLIS = 2000;

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

    @Setter(AccessLevel.PRIVATE)
    private boolean isCompleted;

    public SessionLoginHelper(Session session, String email, String password) {
        this(session, email, password, DEFALUE_LOGIN_TIMEOUT_MILLIS);
    }

    public SessionLoginHelper(Session session, String email, String password, long loginTimeoutMillis) {
        this.session = session;
        this.email = email;
        this.password = password;
        this.loginTimeoutMillis = loginTimeoutMillis;
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
    }

    public void startLogin() throws IOException, SAXException {
        if (isCompleted) throw new IllegalStateException("Already logged in");
        miniLoginPage = webClient.getPage("https://passport.bilibili.com/ajax/miniLogin/minilogin");
        miniLoginPage.getElementById("login-username").setAttribute("value", email);
        miniLoginPage.getElementById("login-passwd").setAttribute("value", password);

        miniLoginPage.getHtmlElementById("login-submit").click();
    }

    public Image getCaptcha() throws IOException {
        if (isCompleted) throw new IllegalStateException("Already logged in");
        HtmlImage image = (HtmlImage) miniLoginPage.getByXPath("//img[@class='captcha-img']").get(0);
        return image.getImageReader().read(0);
    }

    public void loginWithCaptcha(String captcha) throws IOException, SAXException {
        if (isCompleted) throw new IllegalStateException("Already logged in");
        miniLoginPage.getElementById("login-username").setAttribute("value", email);
        miniLoginPage.getElementById("login-passwd").setAttribute("value", password);
        miniLoginPage.getElementById("login-captcha").setAttribute("value", captcha);
        miniLoginPage.getHtmlElementById("login-submit").click();

        //WARNING: USED Experimental API HERE!
        webClient.waitForBackgroundJavaScript(loginTimeoutMillis);

        isCompleted = true;
    }

    public LoginStatus getLoginStatus() {
        if (isCompleted) return NOT_COMPLETED;
        if (checkSuccess())
            return SUCCESS;

        //Due to Java's limit, we must use 1 element array to storage result.
        final LoginStatus[] result = {UNKNOWN};

        //find elements like : <p class="message" for="captcha">验证码错误</p>
        miniLoginPage.getElementsByTagName("p")
                .stream()
                .filter(domElement -> (domElement.getAttribute("class").equals("message")
                        && !domElement.getTextContent().trim().isEmpty()))
                .forEach(domElement ->
                        result[0] = LoginStatus.forName(domElement.getTextContent().trim()));

        return result[0];
    }

    private boolean checkSuccess() {
        //bilibili will open https://passport.bilibili.com/ajax/miniLogin/redirect if login success.
        return webClient.getCurrentWindow().getEnclosedPage().getUrl().toString().contains("redirect");
    }

    public enum LoginStatus {
        NOT_COMPLETED, SUCCESS, UNKNOWN;

        public static LoginStatus forName(String statusDisplayString) {
            return null;
        }
    }
}
