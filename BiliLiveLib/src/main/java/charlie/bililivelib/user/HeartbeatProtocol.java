package charlie.bililivelib.user;

import charlie.bililivelib.I18n;
import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.exceptions.NetworkException;
import charlie.bililivelib.exceptions.NotLoggedInException;
import charlie.bililivelib.internalutil.net.HttpHelper;
import com.google.gson.annotations.SerializedName;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * 用于发送心跳包的协议类。
 * 哔哩哔哩直播若每五分钟发送心跳包会获得3000经验/5min。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class HeartbeatProtocol {
    private static final String ROOM_FULL_URL = "http://live.bilibili.com/2";
    private static final String HEARTBEAT_FULL_URL_G = "http://api.live.bilibili.com/User/userOnlineHeart";
    private static final String EXCEPTION_KEY = "exception.heartbeat";
    private static final int STATUS_NOT_LOGGED_IN = -101;
    private Session session;

    public HeartbeatProtocol(@NotNull Session session) {
        this.session = session;
    }

    /**
     * 发送心跳包。
     * @throws NotLoggedInException 在未登录时抛出
     * @throws NetworkException 在发生网络问题时抛出
     */
    public void heartbeat() throws BiliLiveException {
        try {
            HttpGet httpGet = new HttpGet(HEARTBEAT_FULL_URL_G);
            httpGet.setHeader("Referer", ROOM_FULL_URL);

            HttpResponse response = session.getHttpHelper().getHttpClient().execute(httpGet);
            String jsonString = HttpHelper.responseToString(response);
            HeartbeatResultInfo resultInfo = session.getHttpHelper().responseToObject(
                    response,
                    HeartbeatResultInfo.class,
                    EXCEPTION_KEY);
            if (resultInfo.code == STATUS_NOT_LOGGED_IN) throw new NotLoggedInException();
        } catch (IOException ex) {
            throw new NetworkException(I18n.getString(EXCEPTION_KEY), ex);
        }
    }

    private static class HeartbeatResultInfo {
        @MagicConstant(intValues = {0, -101})
        private int code;
        @SerializedName("msg")
        private String message;
    }
}
