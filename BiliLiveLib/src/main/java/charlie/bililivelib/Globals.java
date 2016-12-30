package charlie.bililivelib;

import charlie.bililivelib.net.BilibiliTrustStrategy;
import charlie.bililivelib.net.HttpHelper;
import com.google.gson.Gson;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class Globals {
    private static final String BILI_PASSPORT_HOST_ROOT = "passport.bilibili.com";
    private static final String BILI_LIVE_HOST_ROOT = "live.bilibili.com";
    private static final int CONNECTION_ALIVE_TIME_SECOND = 10;
    private static Globals instance;
    @Getter
    private HttpHost biliLiveRoot;
    @Getter
    private HttpHost biliPassportHttpsRoot;
    private ThreadLocal<Gson> gson;
    @Getter
    private HttpClientConnectionManager connectionPool;
    @Getter
    private HttpHelper noSessionHttpHelper;
    @Getter
    private SSLContext bilibiliSSLContext;

    public static Globals get() {
        if (instance == null) {
            instance = new Globals();
            instance.init();
        }
        return instance;
    }
    
    public void init() {
        noSessionHttpHelper = new HttpHelper();
        noSessionHttpHelper.init();

        biliLiveRoot = new HttpHost(BILI_LIVE_HOST_ROOT);
        //Visit bilibili passport via https. 443 is the https port.
        biliPassportHttpsRoot = new HttpHost(BILI_PASSPORT_HOST_ROOT, 443, "https");
        gson = ThreadLocal.withInitial(Gson::new);
        connectionPool = new PoolingHttpClientConnectionManager(CONNECTION_ALIVE_TIME_SECOND, TimeUnit.SECONDS);
        try {
            bilibiliSSLContext = SSLContextBuilder.create()
                    .loadTrustMaterial(new BilibiliTrustStrategy())
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new AssertionError("failed initializing bilibili ssl context!");
        }
    }

    public Gson getGson() {
        return gson.get();
    }
}
