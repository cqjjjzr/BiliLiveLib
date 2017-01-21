package charlie.bililivelib.capsuletoy;

import charlie.bililivelib.Globals;
import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.exceptions.NotLoggedInException;
import charlie.bililivelib.net.PostArguments;
import charlie.bililivelib.user.Session;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;
import org.apache.http.cookie.Cookie;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CapsuleToyProtocol {
    private static final String CAPSULE_OPEN_POST = "/api/ajaxCapsuleOpen";
    private static final String CAPSULE_INFO_GET = "/api/ajaxCapsule";
    private static final String EXCEPTION_KEY = "exception.capsule_toy";
    private static final int STATUS_NOT_LOGGED_IN = -101;
    private static final int STATUS_SUCCESS = 0;
    private static final int STATUS_NOT_ENOUGH = -500;
    private static final String TOKEN_COOKIE_KEY = "LIVE_LOGIN_DATA";
    private Session session;

    public CapsuleToyProtocol(@NotNull Session session) {
        this.session = session;
    }

    public OpenCapsuleToyInfo openNormal(@MagicConstant(intValues = {1, 10, 100}) int count) throws BiliLiveException {
        return openCapsule("normal", count, getToken());
    }

    public OpenCapsuleToyInfo openColorful(@MagicConstant(intValues = {1, 10, 100}) int count) throws BiliLiveException {
        return openCapsule("colorful", count, getToken());
    }

    private OpenCapsuleToyInfo openCapsule(String type, int count, String token) throws BiliLiveException {
        JsonObject rootObject = session.getHttpHelper().postBiliLiveJSON(CAPSULE_OPEN_POST,
                new PostArguments()
                        .add("type", type)
                        .add("count", String.valueOf(count))
                        .add("token", token),
                JsonObject.class, EXCEPTION_KEY);
        int code = rootObject.get("code").getAsInt();

        if (rootObject.get("code").getAsInt() == STATUS_NOT_LOGGED_IN) throw new NotLoggedInException();
        if (rootObject.get("code").getAsInt() != STATUS_SUCCESS) return new OpenCapsuleToyInfo(code,
                rootObject.get("msg").getAsString());
        return Globals.get().gson().fromJson(rootObject, OpenCapsuleToyInfo.class);
    }

    public CapsuleToyInfo getCapsuleToyInfo() throws BiliLiveException {
        CapsuleToyInfo info = session.getHttpHelper().getBiliLiveJSON(
                CAPSULE_INFO_GET, CapsuleToyInfo.class, EXCEPTION_KEY);
        if (info.code == STATUS_NOT_LOGGED_IN) throw new NotLoggedInException();
        return info;
    }

    private String getToken() throws BiliLiveException {
        for (Cookie cookie : session.getCookieStore().getCookies()) {
            if (cookie.getName().equals(TOKEN_COOKIE_KEY)) return cookie.getValue();
        }
        throw new NotLoggedInException();
    }

    @Getter
    @ToString
    public static class CapsuleToyInfo {
        private int code;
        @SerializedName("msg")
        private String message;
        private DataBean data;

        public DataBean data() {
            return data;
        }

        public int getAvailableNormal() {
            return data.normal.count;
        }

        public int getAvailableColorful() {
            return data.colorful.count;
        }

        @Getter
        @ToString
        public static class DataBean {
            private CoinBean normal;
            private CoinBean colorful;

            @Getter
            @ToString
            public static class CoinBean {
                @SerializedName("status")
                private boolean available;
                @SerializedName("coin")
                private int count;
                private int change;
                private ProgressBean progress;
                private String rule;
                @SerializedName("gift")
                private List<GiftBean> gettableGift;
                @SerializedName("list")
                private List<ListBean> nameList;

                @Getter
                @ToString
                public static class ProgressBean {
                    private int now;
                    private int max;
                }

                @Getter
                @ToString
                public static class GiftBean {
                    private String id;
                    private String name;
                }

                @Getter
                @ToString
                public static class ListBean {
                    private String num;
                    private String gift;
                    private String dateYYYYMMDD;
                    private String name;
                }
            }
        }
    }

    @Getter
    @ToString
    public static class OpenCapsuleToyInfo {
        private int code;
        @SerializedName("msg")
        private String message;
        private DataBean data;

        public OpenCapsuleToyInfo() {
        }

        public OpenCapsuleToyInfo(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public boolean isSuccessful() {
            return data.successful;
        }

        public boolean hasEnoughCoins() {
            return code != -500;
        }

        @Getter
        @ToString
        public static class DataBean {
            @SerializedName("status")
            private boolean successful;
            @SerializedName("isEntity")
            private boolean entity;
            private List<String> text;

            public String getText() {
                return text.get(0);
            }
        }
    }
}
