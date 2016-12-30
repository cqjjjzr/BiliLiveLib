package charlie.bililivelib.session;

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

@Getter
public class Session {
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
                .setUserAgent("BiliLiveLib " + BiliLiveLib.VERSION)
                .setConnectionManager(clientConnectionManager)
                .setSSLContext(BILIBILI_SSL_CONTEXT)
                .setDefaultCookieStore(cookieStore);
        httpHelper.init(builder.build());
    }

    public void fromXML(String xml) {
        SessionPersistenceHelper.fromXML(this, xml);
    }

    public String toXML() {
        return SessionPersistenceHelper.toXML(this);
    }
}
