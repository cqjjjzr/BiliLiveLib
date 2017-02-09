package charlie.bililivelib.user;

import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.exceptions.NetworkException;
import charlie.bililivelib.exceptions.NotLoggedInException;
import charlie.bililivelib.internalutil.net.HttpHelper;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 用于用户签到的协议类。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class SignProtocol {
    private static final String EXCEPTION_SIGN = "exception.sign";
    private static final String SIGN_INFO_G = "/sign/GetSignInfo";
    private static final String DO_SIGN_IN_G = "/sign/doSign";
    private static final int STATUS_NOT_LOGGED_IN = -105;
    private HttpHelper httpHelper;

    public SignProtocol(@NotNull Session session) {
        httpHelper = session.getHttpHelper();
    }

    /**
     * 签到。
     * @return 签到信息，指示签到是否成功，以及签到获取的奖励
     * @throws NetworkException 在发生网络问题时抛出
     * @throws NotLoggedInException 在未登录时抛出
     */
    public DoSignInfo signIn() throws BiliLiveException {
        DoSignInfo doSignInfo = httpHelper.getBiliLiveJSON(DO_SIGN_IN_G, DoSignInfo.class, EXCEPTION_SIGN);
        if (doSignInfo.getCode() == -101) throw new NotLoggedInException();
        return doSignInfo;
    }

    /**
     * 获取当前签到信息。
     * @return 签到信息
     * @throws NetworkException 在发生网络问题时抛出
     * @throws NotLoggedInException 在未登录时抛出
     */
    public SignInfo getCurrentSignInfo() throws BiliLiveException {
        SignInfo info = httpHelper.getBiliLiveJSON(SIGN_INFO_G, SignInfo.class, EXCEPTION_SIGN);
        if (info.getCode() == STATUS_NOT_LOGGED_IN) throw new NotLoggedInException();
        return info;
    }

    @Getter
    @ToString
    public static class DoSignInfo {
        private static final int SUCCESS = 0;
        private static final int E_ALREADY_SIGNED_IN = -500;

        private int code;
        @SerializedName("msg")
        private String message;
        private Data data;

        public boolean isSuccessful() {
            return code == SUCCESS;
        }

        public boolean isAlreadySignedIn() {
            return code == E_ALREADY_SIGNED_IN;
        }

        public Data data() {
            return data;
        }

        @Getter
        @ToString
        public static class Data {
            private String text;
            private String allDays;
            private int hadSignDays;
            private int remindDays;
        }
    }

    @Getter
    @ToString
    public static class SignInfo {
        private int code;
        @SerializedName("msg")
        private String message;
        private Data data;

        public Data data() {
            return data;
        }

        public boolean isSignedIn() {
            return data.signedDaysList.contains(data.todayDayOfMonth);
        }

        @Getter
        @ToString
        public static class Data {
            private String text;
            private String specialText;
            private int status;
            @SerializedName("allDays")
            private int dayCountOfThisMonth;
            @SerializedName("curMonth")
            private int todayMonthOfYear;
            @SerializedName("curYear")
            private int todayYear;
            @SerializedName("curDay")
            private int todayDayOfMonth;
            @SerializedName("curDate")
            private String todayYearMonthDay;
            private int hadSignDays;
            private int newTask;
            @SerializedName("signDaysList")
            private List<Integer> signedDaysList;
            @SerializedName("signBonusDaysList")
            private List<Integer> signedBonusDaysList;
        }
    }
}
