package charlie.bililivelib.user;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.Globals;
import charlie.bililivelib.net.HttpHelper;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;

@Getter
public class Session {
    public static final String EXIT_URL_G = "/login?act=exit";
    public static final String ACTIVATE_URL = "http://live.bilibili.com/2";

    private static final SSLContext BILIBILI_SSL_CONTEXT = Globals.get().getBilibiliSSLContext();
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
                .setUserAgent(BiliLiveLib.USER_AGENT)
                .setConnectionManager(clientConnectionManager)
                .setSSLContext(BILIBILI_SSL_CONTEXT)
                //.setProxy(new HttpHost("127.0.0.1", 8888)) // For Fiddler Debugging
                .setDefaultCookieStore(cookieStore);
        httpHelper.init(builder.build());
    }

    public void logout() throws IOException {
        httpHelper.executeGet(Globals.get().getBiliPassportHttpsRoot(), EXIT_URL_G);
        cookieStore.clear();
    }

    protected void activate() throws IOException {
        httpHelper.executeBiliLiveGet(ACTIVATE_URL);
    }

    public void fromBase64(String base64) {
        SessionPersistenceHelper.fromBase64(this, base64);
    }

    public String toBase64() {
        return SessionPersistenceHelper.toBase64(this);
    }
}
