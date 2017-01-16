package charlie.bililivelib;

import charlie.bililivelib.net.BilibiliTrustStrategy;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.util.OCRUtil;
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
    private ThreadLocal<Cache> htmlUnitCache;
    private HttpClientConnectionManager connectionPool;
    private HttpHelper noSessionHttpHelper;
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
    
    public void init() {
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
            connectionPool = new PoolingHttpClientConnectionManager(CONNECTION_ALIVE_TIME_SECOND, TimeUnit.SECONDS);
        }
        return connectionPool;
    }

    public OCRUtil getOcrUtil() {
        if (ocrUtil == null) {
            ocrUtil = new OCRUtil();
        }
        return ocrUtil;
    }

    public HttpHelper getNoSessionHttpHelper() {
        if (noSessionHttpHelper == null) {
            noSessionHttpHelper = new HttpHelper();
            noSessionHttpHelper.init();
        }
        return noSessionHttpHelper;
    }
}
