package charlie.bililivelib.user;

import charlie.bililivelib.Globals;
import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.exceptions.NotLoggedInException;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.util.I18n;
import com.google.gson.annotations.SerializedName;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class HeartbeatProtocol {
    private static final String ROOM_FULL_URL = "http://live.bilibili.com/2";
    private static final String HEARTBEAT_FULL_URL_G = "http://api.live.bilibili.com/User/userOnlineHeart";
    private static final String EXCEPTION_KEY = "exception.heartbeat";
    private static final int STATUS_NOT_LOGGED_IN = -101;
    private Session session;

    public HeartbeatProtocol(@NotNull Session session) {
        this.session = session;
    }

    public void heartbeat() throws BiliLiveException {
        try {
            HttpGet httpGet = new HttpGet(HEARTBEAT_FULL_URL_G);
            httpGet.setHeader("Referer", ROOM_FULL_URL);

            HttpResponse response = session.getHttpHelper().getHttpClient().execute(httpGet);
            String jsonString = HttpHelper.responseToString(response);
            HeartbeatResultInfo resultInfo = Globals.get().gson().fromJson(jsonString, HeartbeatResultInfo.class);
            if (resultInfo.code == STATUS_NOT_LOGGED_IN) throw new NotLoggedInException();
        } catch (IOException ex) {
            throw new BiliLiveException(I18n.getString(EXCEPTION_KEY), ex);
        }
    }

    private static class HeartbeatResultInfo {
        @MagicConstant(intValues = {0, -101})
        private int code;
        @SerializedName("msg")
        private String message;
    }
}
