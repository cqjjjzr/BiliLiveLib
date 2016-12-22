package charlie.bililivelib;

import charlie.bililivelib.net.HttpHelper;
import com.google.gson.Gson;
import lombok.Getter;
import org.apache.http.HttpHost;

public class GlobalObjects {
    private static final String BILI_LIVE_HOST_ROOT = "live.bilibili.com";

    @Getter
    private HttpHost biliLiveRoot;
    @Getter
    private HttpHelper httpHelper;
    private ThreadLocal<Gson> gson;

    private static GlobalObjects instance;

    public void init() {
        httpHelper = new HttpHelper();
        httpHelper.init();

        biliLiveRoot = new HttpHost(BILI_LIVE_HOST_ROOT);
        gson = ThreadLocal.withInitial(Gson::new);
    }
    
    public void getGson() {
        return gson.get();
    }

    public static GlobalObjects instance() {
        if (instance == null) {
            instance = new GlobalObjects();
            instance.init();
        }
        return instance;
    }
}
