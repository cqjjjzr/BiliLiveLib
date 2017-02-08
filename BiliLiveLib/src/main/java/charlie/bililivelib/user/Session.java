package charlie.bililivelib.user;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.Globals;
import charlie.bililivelib.net.HttpHelper;
import lombok.Getter;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;

/**
 * 本类用于存放用户会话。登录回话请参看{@link charlie.bililivelib.user.SessionLoginHelper}
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Getter
public class Session {
    private static final String EXIT_URL_G = "/login?act=exit";
    private static final String ACTIVATE_URL = "http://live.bilibili.com/2";

    private static final SSLContext BILIBILI_SSL_CONTEXT = Globals.get().getBilibiliSSLContext();
    private HttpHelper httpHelper;
    @Getter
    private CookieStore cookieStore;

    public Session() {
        this(Globals.get().getConnectionPool());
    }

    public Session(String userAgent) {
        this(Globals.get().getConnectionPool(), userAgent);
    }

    public Session(HttpClient httpClient, CookieStore cookieStore) {
        httpHelper = new HttpHelper();
        httpHelper.init(httpClient);
        this.cookieStore = cookieStore;
    }

    public Session(HttpClientConnectionManager clientConnectionManager) {
        this(clientConnectionManager, BiliLiveLib.DEFAULT_USER_AGENT);
    }

    public Session(HttpClientConnectionManager clientConnectionManager, String userAgent) {
        httpHelper = new HttpHelper();
        initHttpHelper(clientConnectionManager, userAgent);
    }

    private void initHttpHelper(HttpClientConnectionManager clientConnectionManager, String userAgent) {
        cookieStore = new BasicCookieStore();
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setUserAgent(userAgent)
                .setConnectionManager(clientConnectionManager)
                .setSSLContext(BILIBILI_SSL_CONTEXT)
                //.setProxy(new HttpHost("127.0.0.1", 8888)) // For Fiddler Debugging
                .setDefaultCookieStore(cookieStore);
        httpHelper.init(builder.build());
    }

    /**
     * 登出会话。
     * @throws IOException 出现网络问题时抛出
     */
    public void logout() throws IOException {
        httpHelper.executeGet(Globals.get().getBiliPassportHttpsRoot(), EXIT_URL_G);
        cookieStore.clear();
    }

    protected void activate() throws IOException {
        httpHelper.executeBiliLiveGet(ACTIVATE_URL);
    }

    /**
     * 从Base64字符串恢复到会话。
     * @param base64 Base64流
     */
    public void fromBase64(String base64) {
        SessionPersistenceHelper.fromBase64(this, base64);
    }

    /**
     * 将会话序列化到Base64字符串。
     * @return 该会话的Base64字符串
     */
    public String toBase64() {
        return SessionPersistenceHelper.toBase64(this);
    }
}
