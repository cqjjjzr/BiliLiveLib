package charlie.bililivelib;

import charlie.bililivelib.internalutil.OCRUtil;
import charlie.bililivelib.internalutil.net.BilibiliTrustStrategy;
import com.gargoylesoftware.htmlunit.Cache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

/**
 * 本单例类定义了一组在整个应用程序中的全局变量，这些变量仅用于Library内部使用且随时可能修改，请勿随意访问该类中的对象。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class Globals {
    private static final String BILI_PASSPORT_HOST_ROOT = "passport.bilibili.com";
    private static final String BILI_LIVE_HOST_ROOT = "live.bilibili.com";
    @Getter
    private static int CONNECTION_ALIVE_TIME_SECOND = 10;
    private static Globals instance;
    @Getter
    private HttpHost biliLiveRoot;
    @Getter
    private HttpHost biliPassportHttpsRoot;
    private ThreadLocal<Gson> gson;
    private ThreadLocal<Cache> htmlUnitCache;
    private HttpClientConnectionManager connectionPool;
    @Getter
    private SSLContext bilibiliSSLContext;
    private OCRUtil ocrUtil;

    public static Globals get() {
        if (instance == null) {
            instance = new Globals();
            instance.init();
        }
        return instance;
    }

    private void init() {
        biliLiveRoot = new HttpHost(BILI_LIVE_HOST_ROOT);
        //Visit bilibili passport via https. 443 is the https port.
        biliPassportHttpsRoot = new HttpHost(BILI_PASSPORT_HOST_ROOT, 443, "https");
        gson = ThreadLocal.withInitial(() -> new GsonBuilder()
                .setLenient()
                .create());
        htmlUnitCache = ThreadLocal.withInitial(Cache::new);
        try {
            bilibiliSSLContext = SSLContextBuilder.create()
                    .loadTrustMaterial(new BilibiliTrustStrategy())
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new AssertionError("failed initializing bilibili ssl context!");
        }
    }

    public Gson gson() {
        return gson.get();
    }

    public Cache getHtmlUnitCache() {
        return htmlUnitCache.get();
    }

    public HttpClientConnectionManager getConnectionPool() {
        if (connectionPool == null) {
            reInitializeConnectionPool();
        }
        return connectionPool;
    }

    public void reInitializeConnectionPool() {
        connectionPool = new PoolingHttpClientConnectionManager(CONNECTION_ALIVE_TIME_SECOND, TimeUnit.SECONDS);
    }

    public void setConnectionAliveTimeSecond(int connectionAliveTimeSecond) {
        CONNECTION_ALIVE_TIME_SECOND = connectionAliveTimeSecond;
        reInitializeConnectionPool();
    }

    public OCRUtil getOcrUtil() {
        if (ocrUtil == null) {
            ocrUtil = new OCRUtil();
        }
        return ocrUtil;
    }
}
