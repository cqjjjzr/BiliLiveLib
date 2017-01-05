package charlie.bililivelib.session;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.xml.sax.SAXException;

import java.awt.*;
import java.io.IOException;

import static charlie.bililivelib.session.SessionLoginHelper.LoginStatus.SUCCESS;
import static charlie.bililivelib.session.SessionLoginHelper.LoginStatus.UNKNOWN;

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

    public SessionLoginHelper(Session session, String email, String password) {
        this(session, email, password, DEFALUE_LOGIN_TIMEOUT_MILLIS);
    }

    public SessionLoginHelper(Session session, String email, String password, long loginTimeoutMillis) {
        this.session = session;
        this.email = email;
        this.password = password;
        this.loginTimeoutMillis = loginTimeoutMillis;
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        avoidUselessErrorMessages();
    }

    public void startLogin() throws IOException, SAXException {
        miniLoginPage = webClient.getPage("https://passport.bilibili.com/ajax/miniLogin/minilogin");
        miniLoginPage.getElementById("login-username").setAttribute("value", email);
        miniLoginPage.getElementById("login-passwd").setAttribute("value", password);
        miniLoginPage.getHtmlElementById("login-submit").click();
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

    public Image getCaptcha() throws IOException {
        HtmlImage image = (HtmlImage) miniLoginPage.getByXPath("//img[@class='captcha-img']").get(0);
        return image.getImageReader().read(0);
    }

    public void loginWithCaptcha(String captcha) throws IOException, SAXException {
        miniLoginPage.getElementById("login-username").setAttribute("value", email);
        miniLoginPage.getElementById("login-passwd").setAttribute("value", password);
        miniLoginPage.getElementById("login-captcha").setAttribute("value", captcha);
        miniLoginPage.getHtmlElementById("login-submit").click();

        //WARNING: USED Experimental API HERE!
        webClient.waitForBackgroundJavaScript(loginTimeoutMillis);
    }

    public LoginStatus getLoginStatus() {
        if (checkSuccess())
            return SUCCESS;

        LoginStatus result = UNKNOWN;

        for (DomElement domElement : miniLoginPage.getElementsByTagName("p")) {
            if (isValidMessageElement(domElement)) {
                result = LoginStatus.forName(domElement.getTextContent().trim());
            }
        }

        return result;
    }

    private boolean isValidMessageElement(DomElement domElement) {
        return domElement.getAttribute("class").equals("message")
                && !domElement.getTextContent().trim().isEmpty();
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
