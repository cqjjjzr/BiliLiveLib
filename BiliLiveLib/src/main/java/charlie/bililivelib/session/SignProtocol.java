package charlie.bililivelib.session;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.net.HttpHelper;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

public class SignProtocol {
    private static final String EXCEPTION_SIGN = "exception.sign";
    private static final String SIGN_INFO_G = "/sign/GetSignInfo";
    private static final String DO_SIGN_IN_G = "/sign/doSign";
    private HttpHelper httpHelper;

    public SignProtocol(Session session) {
        httpHelper = session.getHttpHelper();
    }

    public DoSignInfo signIn() throws BiliLiveException {
        return httpHelper.getBiliLiveJSON(DO_SIGN_IN_G, DoSignInfo.class, EXCEPTION_SIGN);
    }

    public SignInfo getCurrentSignInfo() throws BiliLiveException {
        return httpHelper.getBiliLiveJSON(SIGN_INFO_G, SignInfo.class, EXCEPTION_SIGN);
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
