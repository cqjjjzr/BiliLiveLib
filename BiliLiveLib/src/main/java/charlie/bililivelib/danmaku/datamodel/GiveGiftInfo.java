package charlie.bililivelib.danmaku.datamodel;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class GiveGiftInfo {
    @SerializedName("data")
    private DataBean content;

    @Data
    public static class DataBean {
        private String giftName;
        @SerializedName("num")
        private int count;
        @SerializedName("uname")
        private String username;
        private int uid;
        @SerializedName("giftId")
        private int giftID;
        private int giftType;
        private int price;
    }
}
