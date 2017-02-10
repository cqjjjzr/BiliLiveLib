package charlie.bililivelib.danmaku.datamodel;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 用于存放送礼触发的全屏公告的信息。
 *
 * @author Charlie Jiang
 * @since rv1
 */
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
