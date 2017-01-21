package charlie.bililivelib.net;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.Globals;
import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.util.I18n;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class HttpHelper {
    private final SSLContext bilibiliSSLContext = Globals.get().getBilibiliSSLContext();
    private final HttpHost biliLiveRoot = Globals.get().getBiliLiveRoot();
    @Getter
    private HttpClient httpClient;

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

    public void init() {
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setUserAgent(BiliLiveLib.USER_AGENT)
                .setProxy(new HttpHost("127.0.0.1", 8888))
                .setSSLContext(bilibiliSSLContext);
        init(builder.build());
    }

    public void init(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse createPostBiliLiveHost(String url, PostArguments args) throws IOException {
        return createPostResponse(biliLiveRoot, url, args);
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

    public HttpResponse createGetBiliLiveHost(String url) throws IOException {
        return createGetResponse(biliLiveRoot, url);
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

    public <T> T getJSON(HttpHost httpHost, String url, Class<T> clazz, String exceptionKey) throws BiliLiveException {
        try {
            HttpResponse response = this.createGetResponse(httpHost, url);
            return responseToObject(response, clazz, exceptionKey);
        } catch (IOException e) {
            throw new BiliLiveException(I18n.getString(exceptionKey), e);
        }
    }

    public <T> T getBiliLiveJSON(String url, Class<T> clazz, String exceptionKey) throws BiliLiveException {
        return getJSON(biliLiveRoot, url, clazz, exceptionKey);
    }

    public <T> T postJSON(HttpHost httpHost, String url, PostArguments arguments,
                          Class<T> clazz, String exceptionKey) throws BiliLiveException {
        try {
            HttpResponse response = this.createPostResponse(httpHost, url, arguments);
            return responseToObject(response, clazz, exceptionKey);
        } catch (IOException e) {
            throw new BiliLiveException(I18n.getString(exceptionKey), e);
        }
    }

    public <T> T postBiliLiveJSON(String url, PostArguments arguments,
                                  Class<T> clazz, String exceptionKey) throws BiliLiveException {
        return postJSON(biliLiveRoot, url, arguments, clazz, exceptionKey);
    }

    public <T> T responseToObject(HttpResponse response, Class<T> clazz, String exceptionKey) throws IOException {
        String jsonString = HttpHelper.responseToString(response);

        return Globals.get().gson().fromJson(jsonString, clazz);
    }

    public void executeGet(HttpHost httpHost, String url) throws IOException {
        EntityUtils.consume(this.createGetResponse(httpHost, url).getEntity());
    }

    public void executeBiliLiveGet(String url) throws IOException {
        executeGet(biliLiveRoot, url);
    }
}
