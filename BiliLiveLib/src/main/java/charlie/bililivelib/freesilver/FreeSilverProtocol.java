package charlie.bililivelib.freesilver;

import charlie.bililivelib.Globals;
import charlie.bililivelib.I18n;
import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.exceptions.NotLoggedInException;
import charlie.bililivelib.exceptions.WrongCaptchaException;
import charlie.bililivelib.internalutil.CaptchaUtil;
import charlie.bililivelib.internalutil.MiscUtil;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.user.Session;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.MessageFormat;

public class FreeSilverProtocol {
    private static final String GET_CURRENT_TASK_G = "/FreeSilver/getCurrentTask";
    private static final String GET_TODAY_INFO_G = "/FreeSilver/getTaskInfo";
    private static final String CAPTCHA_G = "/freeSilver/getCaptcha";
    private static final String GET_AWARD_PATTERN_G =
            "/FreeSilver/getAward?time_start={0,number,###}&time_end={1,number,###}&captcha={2}&_={3,number,###}";
    private static final String EXCEPTION_KEY = "exception.freeSilver";
    private static final int _1_KB = 1024;
    private static final String MIME_JPEG = "image/jpeg";
    private static final int STATUS_NOT_LOGIN = -101;
    private HttpHelper httpHelper;

    public FreeSilverProtocol(Session session) {
        httpHelper = session.getHttpHelper();
    }

    public CurrentSilverTaskInfo getCurrentFreeSilverStatus() throws BiliLiveException {
        return httpHelper.getBiliLiveJSON(GET_CURRENT_TASK_G, CurrentSilverTaskInfo.class, EXCEPTION_KEY);
    }

    public FreeSilverTaskInfo getTodayFreeSilverStatus() throws BiliLiveException {
        return httpHelper.getBiliLiveJSON(GET_TODAY_INFO_G, FreeSilverTaskInfo.class, EXCEPTION_KEY);
    }

    public BufferedImage getCaptcha() throws BiliLiveException {
        try {
            HttpResponse response = httpHelper.createGetBiliLiveHost(CAPTCHA_G);
            if (!response.getEntity().getContentType().getValue().equals(MIME_JPEG)) { // Returned a JSON instead of an image
                String jsonString = HttpHelper.responseToString(response);
                JsonObject rootObject = Globals.get().gson().fromJson(jsonString, JsonObject.class);
                if (rootObject.get("code").getAsInt() == STATUS_NOT_LOGIN)
                    throw new NotLoggedInException();
                throw new BiliLiveException(I18n.format("freeSilver.captcha",
                        Globals.get().gson().fromJson(jsonString, JsonObject.class)));
            }
            return ImageIO.read(HttpHelper.responseToInputStream(response));
        } catch (IOException e) {
            throw new BiliLiveException(EXCEPTION_KEY, e);
        }
    }

    public String ocrCaptcha(BufferedImage image) {
        return String.valueOf(new CaptchaUtil(Globals.get().getOcrUtil()).evalCalcCaptcha(image));
    }

    public GetSilverInfo waitToGetSilver() throws BiliLiveException, InterruptedException {
        return waitToGetSilver(getCurrentFreeSilverStatus());
    }

    public GetSilverInfo waitToGetSilver(CurrentSilverTaskInfo currentInfo) throws
            BiliLiveException, InterruptedException {
        if (currentInfo.status() != CurrentSilverTaskInfo.Status.REMAINING)
            throw new BiliLiveException(I18n.format("freeSilver.status", currentInfo.status()));

        long time = getWaitTime(currentInfo);
        if (time > 0)
            Thread.sleep(time);

        while (true) {
            GetSilverInfo info = getNowSilver(
                    currentInfo.data.getWaitingStartUnixTimestamp(),
                    currentInfo.data.getWaitingEndUnixTimestamp(), ocrCaptcha(getCaptcha()));
            if (info.status() == GetSilverInfo.Status.SUCCESS) return info;
            if (info.status() == GetSilverInfo.Status.EXPIRE) {
                if (info.data.surplusMinute < 0) throw new BiliLiveException(I18n.getString("freeSilver.expire"));
                MiscUtil.sleepMillis((long) (info.data.surplusMinute * 60 * 1000));
            }
        }
    }

    private long getWaitTime(CurrentSilverTaskInfo currentInfo) {
        return ((long) currentInfo.data.getWaitingEndUnixTimestamp() * 1000) - System.currentTimeMillis();
    }

    public GetSilverInfo getNowSilver() throws BiliLiveException {
        CurrentSilverTaskInfo currentInfo = getCurrentFreeSilverStatus();

        return getNowSilver(
                currentInfo.data.getWaitingStartUnixTimestamp(),
                currentInfo.data.getWaitingEndUnixTimestamp(),
                ocrCaptcha(getCaptcha()));
    }

    public GetSilverInfo getNowSilver(long timeStart, long timeEnd, String captcha) throws BiliLiveException {
        GetSilverInfo info = httpHelper.getBiliLiveJSON(
                generateGetSilverRequest(timeStart, timeEnd, captcha), GetSilverInfo.class, EXCEPTION_KEY);
        if (info.status() == GetSilverInfo.Status.WRONG_OR_EXPIRED_CAPTCHA) throw new WrongCaptchaException();
        if (info.status() == GetSilverInfo.Status.NOT_LOGGED_IN) throw new NotLoggedInException();
        return info;
    }

    @NotNull
    @Contract(pure = true)
    private String generateGetSilverRequest(long timeStart, long timeEnd, String captcha) {
        return MessageFormat.format(GET_AWARD_PATTERN_G,
                timeStart,
                timeEnd,
                captcha,
                System.currentTimeMillis());
    }

    @Getter
    @ToString
    public static class GetSilverInfo {
        private int code;
        @SerializedName("msg")
        private String message;
        private Data data;

        public Data data() {
            return data;
        }

        public boolean isEnd() {
            return data.end == 1;
        }

        public Status status() {
            return Status.forCode(code);
        }

        public enum Status {
            SUCCESS(0), EXPIRE(-99), WRONG_OR_EXPIRED_CAPTCHA(-400), NOT_LOGGED_IN(-101), EMPTY(-10017), UNKNOWN(Integer.MIN_VALUE);

            private int code;

            Status(int code) {
                this.code = code;
            }

            public static Status forCode(int code) {
                for (Status status : Status.values()) {
                    if (status.code == code) return status;
                }
                return UNKNOWN;
            }
        }

        @Getter
        @ToString
        public static class Data {
            @SerializedName("silver")
            private int silverID;
            @SerializedName("awardSilver")
            private int awardSilverCount;
            @SerializedName("isEnd")
            private int end;
            @SerializedName("surplus")
            private double surplusMinute;
        }
    }

    @Getter
    @ToString
    public static class CurrentSilverTaskInfo {
        private static final int CODE_REMAINING = 0;
        private int code;
        @SerializedName("msg")
        private String message;
        private Data data;

        public Data data() {
            return data;
        }

        public boolean hasRemaining() {
            return code == CODE_REMAINING;
        }

        public Status status() {
            return Status.forCode(code);
        }

        public enum Status {
            REMAINING(0), FINISHED(-10017), UNKNOWN(Integer.MIN_VALUE);

            @Getter
            private int code;

            Status(int code) {
                this.code = code;
            }

            public static Status forCode(int code) {
                for (Status status : Status.values())
                    if (status.code == code) return status;
                return UNKNOWN;
            }
        }

        @ToString
        @Getter
        public static class Data {
            private int minute;
            @SerializedName("silver")
            private int silverCount;
            @SerializedName("time_start")
            private int waitingStartUnixTimestamp;
            @SerializedName("time_end")
            private int waitingEndUnixTimestamp;
        }
    }

    @Getter
    @ToString
    public static class FreeSilverTaskInfo {
        private int code;
        @SerializedName("msg")
        private String message;
        private Data data;

        public Data data() {
            return data;
        }

        @Getter
        @ToString
        public static class Data {
            @SerializedName("silver")
            private int silverCount;
            @SerializedName("times")
            private int round;
            @SerializedName("type")
            private int typeOfRound;
            private int status;
            @SerializedName("max_times")
            private int maxRounds;
            private int minute;
        }
    }
}
