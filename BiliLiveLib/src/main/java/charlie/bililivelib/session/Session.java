package charlie.bililivelib.session;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.Globals;
import charlie.bililivelib.net.HttpHelper;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;

@Getter
public class Session {
    private static final SSLContext BILIBILI_SSL_CONTEXT = Globals.get().getBilibiliSSLContext();
    private HttpHelper httpHelper;
    @Getter(AccessLevel.PROTECTED)
    private CookieStore cookieStore;

    public Session() {
        this(Globals.get().getConnectionPool());
    }

    public Session(HttpClient httpClient, CookieStore cookieStore) {
        httpHelper = new HttpHelper();
        httpHelper.init(httpClient);
        this.cookieStore = cookieStore;
    }

    public Session(HttpClientConnectionManager clientConnectionManager) {
        httpHelper = new HttpHelper();
        initHttpHelper(clientConnectionManager);
    }

    private void initHttpHelper(HttpClientConnectionManager clientConnectionManager) {
        cookieStore = new BasicCookieStore();
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setUserAgent("BiliLiveLib " + BiliLiveLib.VERSION)
                .setConnectionManager(clientConnectionManager)
                .setSSLContext(BILIBILI_SSL_CONTEXT)
                .setDefaultCookieStore(cookieStore);
        httpHelper.init(builder.build());
    }

    public void fromXML(String xml) {
        SessionPersistenceHelper.fromXML(this, xml);
    }

    public String toXML() {
        return SessionPersistenceHelper.toXML(this);
    }

    public static class LoginResult {
        @Getter
        @Setter
        @SerializedName("status")
        private boolean success;
        @Setter(AccessLevel.PRIVATE)
        @Getter(AccessLevel.PRIVATE)
        private DataBean data;
        @Setter(AccessLevel.PRIVATE)
        @Getter(AccessLevel.PRIVATE)
        private DataBean message;

        public Status getStatus() {
            if (data != null) {
                return Status.forCode(data.getCode());
            }
            return Status.forCode(message.getCode());
        }

        public String getCrossDomainInfoURL() {
            if (data != null) {
                return data.getCrossDomainInfoURL();
            }
            return message.getCrossDomainInfoURL();
        }

        public enum Status {
            SUCCESS(0), CAPTCHA_ERROR(-105), UNKNOWN(-1);

            @Getter
            private int code;

            Status(int code) {
                this.code = code;
            }

            public static Status forCode(int id) {
                for (Status status : Status.values()) {
                    if (status.getCode() == id) return status;
                }
                return UNKNOWN;
            }
        }

        @Data
        public static class DataBean {
            private int code;
            @SerializedName("crossDomain")
            private String crossDomainInfoURL;
        }
    }

    @Data
    private class RSAKeyInfo {
        @SerializedName("hash")
        private String sign;
        private String key;
    }
}
