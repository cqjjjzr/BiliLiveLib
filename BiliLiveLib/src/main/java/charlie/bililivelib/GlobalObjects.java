package charlie.bililivelib;

import charlie.bililivelib.net.HttpHelper;
import com.google.gson.Gson;
import lombok.Getter;
import org.apache.http.HttpHost;

@Getter
public class GlobalObjects {
    private static final String BILI_LIVE_HOST_ROOT = "live.bilibili.com";

    private HttpHost biliLiveRoot;
    private HttpHelper httpHelper;
    private Gson gson;

    private static GlobalObjects instance;

    public void init() {
        httpHelper = new HttpHelper();
        httpHelper.init();

        biliLiveRoot = new HttpHost(BILI_LIVE_HOST_ROOT);
        gson = new Gson();
    }

    public static GlobalObjects instance() {
        if (instance == null) {
            instance = new GlobalObjects();
            instance.init();
        }
        return instance;
    }
}
