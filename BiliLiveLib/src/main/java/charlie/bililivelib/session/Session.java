package charlie.bililivelib.session;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.net.HttpHelper;
import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;

@Getter
public class Session {
    private HttpHelper httpHelper;

    public Session(HttpClient httpClient) {
        httpHelper = new HttpHelper();
        httpHelper.init(httpClient);
    }

    public Session(HttpClientConnectionManager clientConnectionManager) {
        httpHelper = new HttpHelper();
        initHttpHelper(clientConnectionManager);
    }

    private void initHttpHelper(HttpClientConnectionManager clientConnectionManager) {
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setUserAgent("BiliLiveLib " + BiliLiveLib.VERSION)
                .setConnectionManager(clientConnectionManager);
        httpHelper.init(builder.build());
    }
}
