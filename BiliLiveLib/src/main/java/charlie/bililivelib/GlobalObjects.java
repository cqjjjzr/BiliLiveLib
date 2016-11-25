package charlie.bililivelib;

import charlie.bililivelib.net.HttpHelper;
import lombok.Getter;
import org.apache.http.HttpHost;

@Getter
public class GlobalObjects {
    private static final String BILI_LIVE_HOST_ROOT = "live.bilibili.com";

    private HttpHost biliLiveRoot;
    private HttpHelper httpHelper;

    public void init() {
        httpHelper = new HttpHelper();
        httpHelper.init();

        biliLiveRoot = new HttpHost(BILI_LIVE_HOST_ROOT);
    }
}
