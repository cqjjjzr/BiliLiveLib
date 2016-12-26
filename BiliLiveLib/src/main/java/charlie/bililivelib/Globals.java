package charlie.bililivelib;

import charlie.bililivelib.net.HttpHelper;
import com.google.gson.Gson;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

public class Globals {
    private static final String BILI_LIVE_HOST_ROOT = "live.bilibili.com";
    private static final int CONNECTION_ALIVE_TIME_SECOND = 10;

    @Getter
    private HttpHost biliLiveRoot;
    private ThreadLocal<Gson> gson;
    @Getter
    private HttpClientConnectionManager connectionPool;
    @Getter
    private HttpHelper noSessionHttpHelper;

    private static Globals instance;

    public void init() {
        noSessionHttpHelper = new HttpHelper();
        noSessionHttpHelper.init();

        biliLiveRoot = new HttpHost(BILI_LIVE_HOST_ROOT);
        gson = ThreadLocal.withInitial(Gson::new);
        connectionPool = new PoolingHttpClientConnectionManager(CONNECTION_ALIVE_TIME_SECOND, TimeUnit.SECONDS);
    }
    
    public Gson getGson() {
        return gson.get();
    }

    public static Globals get() {
        if (instance == null) {
            instance = new Globals();
            instance.init();
        }
        return instance;
    }
}
