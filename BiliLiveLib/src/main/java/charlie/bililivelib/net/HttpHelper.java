package charlie.bililivelib.net;

import charlie.bililivelib.BiliLiveLib;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class HttpHelper {
    private HttpClient httpClient;

    public void init() {
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setUserAgent("BiliLiveLib " + BiliLiveLib.VERSION);
        init(builder.build());
    }

    public void init(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse createPostResponse(HttpHost host, String url, PostArguments args) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(args.toEntity());

        return httpClient.execute(host, httpPost);
    }

    public HttpResponse createGetResponse(HttpHost host, String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);

        return httpClient.execute(host, httpGet);
    }

    public HttpResponse createGetResponse(URL url) throws IOException {
        HttpGet httpGet;
        try {
            httpGet = new HttpGet(url.toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        return httpClient.execute(httpGet);
    }

    public static int getStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    public static InputStream responseToInputStream(HttpResponse response) throws IOException {
        return response.getEntity().getContent();
    }

    public static String responseToString(HttpResponse response) throws IOException {
        String str = EntityUtils.toString(response.getEntity());
        EntityUtils.consume(response.getEntity());
        return str;
    }
}
