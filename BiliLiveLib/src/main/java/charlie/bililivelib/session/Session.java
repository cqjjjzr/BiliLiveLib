package charlie.bililivelib.session;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.Globals;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.net.PostArguments;
import charlie.bililivelib.util.I18n;
import charlie.bililivelib.util.RSAUtil;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;

@Getter
public class Session {
    private static final SSLContext BILIBILI_SSL_CONTEXT = Globals.get().getBilibiliSSLContext();
    private static final String CAPTCHA_GET = "/captcha?_=";
    private static final String RSA_KEY_GET = "/login?act=getkey&_=";
    private static final String LOGIN_POST = "https://passport.bilibili.com/login/dologin";
    private static final String ACTIVATE_BUVID_GET =
            "/v/web/web_page_view?mid=&fts={0,number,000}" +
                    "&url=https%253A%252F%252Fpassport.bilibili.com%252Flogin%253Fgourl%253Dhttps%25253A%25252F%25252Fpassport.bilibili.com%25252Fsite%25252Fsite.html&proid=1&ptype=1&module=passport" +
                    "&title=%E5%93%94%E5%93%A9%E5%93%94%E5%93%A9%E5%BC%B9%E5%B9%95%E8%A7%86%E9%A2%91%E7%BD%91_-_(_%E3%82%9C-_%E3%82%9C)%E3%81%A4%E3%83%AD_%E4%B9%BE%E6%9D%AF~_-_bilibili&ajaxtag=&ajaxid=&page_ref=https%3A%2F%2Fpassport.bilibili.com%2Fsite%2Fsite.html&_={1,number,000}";
    private static final String ACTIVATE_COOKIES_GET = "/captcha/dfc";
    private static final String ACTIVATE_LOGIN_GET = "/login?gourl=https%3A%2F%2Fpassport.bilibili.com%2Fsite%2Fsite.html";
    private HttpHelper httpHelper;
    @Getter(AccessLevel.PROTECTED)
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
                .setSSLContext(BILIBILI_SSL_CONTEXT)
                .setDefaultCookieStore(cookieStore);
        httpHelper.init(builder.build());
    }

    public Image getCaptcha() throws BiliLiveException {
        try {
            //activateCookies();
            //↑导致404

            HttpResponse response = httpHelper.createGetResponse(Globals.get().getBiliPassportHttpsRoot(),
                    generateCaptchaGet(CAPTCHA_GET));
            return ImageIO.read(HttpHelper.responseToInputStream(response));
        } catch (IOException e) {
            throw BiliLiveException.createCausedException(I18n.getString("session.exception_login"), e);
        }
    }

    private String generateCaptchaGet(String captchaGet) {
        return captchaGet + System.currentTimeMillis();
    }

    public LoginResult login(String emailOrUsername, String password) throws BiliLiveException {
        return login(emailOrUsername, password, "");
    }

    public LoginResult login(String emailOrUsername, String password, String captcha) throws BiliLiveException {
        try {
            RSAKeyInfo rsaKeyInfo = getRSAKeyInfo();
            String passwordPost = generateRSAPassword(password, rsaKeyInfo);

            HttpPost httpPost = new HttpPost(LOGIN_POST);
            httpPost.setEntity(new PostArguments()
                    .add("act", "login")
                    .add("gourl", "")
                    .add("keeptime", String.valueOf(2592000))
                    .add("userid", emailOrUsername)
                    .add("pwd", passwordPost)
                    .add("vdcode", captcha)
                    .toEntity());

            HttpResponse response = httpHelper.getHttpClient().execute(httpPost);
            System.out.println(HttpHelper.responseToString(response));
            return null;
            //return Globals.get().getGson()
            //.fromJson(HttpHelper.responseToString(response), LoginResult.class);
        } catch (IOException e) {
            throw BiliLiveException.createCausedException(I18n.getString("session.exception_login"), e);
        }
    }

    private void activateCookies() throws IOException {
        BasicClientCookie ftsCookie = new BasicClientCookie("fts",
                String.valueOf(System.currentTimeMillis() / 1000));
        ftsCookie.setDomain("passport.bilibili.com");
        cookieStore.addCookie(ftsCookie);

        EntityUtils.consume(httpHelper.createPostResponse(Globals.get().getBiliPassportHttpsRoot(),
                ACTIVATE_COOKIES_GET, new PostArguments()).getEntity());
        EntityUtils.consume(httpHelper.createGetResponse(Globals.get().getBiliPassportHttpsRoot(),
                ACTIVATE_LOGIN_GET).getEntity());

        EntityUtils.consume(httpHelper.createGetResponse(new HttpHost("data.bilibili.com", 443, "https"),
                MessageFormat.format(ACTIVATE_BUVID_GET,
                        System.currentTimeMillis() / 1000,
                        System.currentTimeMillis())).getEntity());
    }

    private String generateRSAPassword(String password, RSAKeyInfo rsaKeyInfo) throws BiliLiveException {
        String saltPassword = password + rsaKeyInfo.getSign();
        return RSAUtil.encrypt(saltPassword.getBytes(), removeKeyHeadTail(rsaKeyInfo.getKey()));
    }

    private String removeKeyHeadTail(String key) {
        return key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "");
    }

    private RSAKeyInfo getRSAKeyInfo() throws IOException {
        HttpResponse response = httpHelper.createGetResponse(Globals.get().getBiliPassportHttpsRoot(),
                generateRSAKeyPost());
        return Globals.get().getGson().fromJson(
                HttpHelper.responseToString(response),
                RSAKeyInfo.class
        );
    }

    private String generateRSAKeyPost() {
        return RSA_KEY_GET + System.currentTimeMillis();
    }

    public void fromXML(String xml) {
        SessionPersistenceHelper.fromXML(this, xml);
    }

    public String toXML() {
        return SessionPersistenceHelper.toXML(this);
    }

    public static class LoginResult {
        @Getter
        @Setter
        @SerializedName("status")
        private boolean success;
        @Setter(AccessLevel.PRIVATE)
        @Getter(AccessLevel.PRIVATE)
        private DataBean data;
        @Setter(AccessLevel.PRIVATE)
        @Getter(AccessLevel.PRIVATE)
        private DataBean message;

        public Status getStatus() {
            if (data != null) {
                return Status.forCode(data.getCode());
            }
            return Status.forCode(message.getCode());
        }

        public String getCrossDomainInfoURL() {
            if (data != null) {
                return data.getCrossDomainInfoURL();
            }
            return message.getCrossDomainInfoURL();
        }

        public enum Status {
            SUCCESS(0), CAPTCHA_ERROR(-105), UNKNOWN(-1);

            @Getter
            private int code;

            Status(int code) {
                this.code = code;
            }

            public static Status forCode(int id) {
                for (Status status : Status.values()) {
                    if (status.getCode() == id) return status;
                }
                return UNKNOWN;
            }
        }

        @Data
        public static class DataBean {
            private int code;
            @SerializedName("crossDomain")
            private String crossDomainInfoURL;
        }
    }

    @Data
    private class RSAKeyInfo {
        @SerializedName("hash")
        private String sign;
        private String key;
    }
}
