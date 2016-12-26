package charlie.bililivelib.room.datamodel;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@Data
public class RoomInfoResponseJson {
    private int code;
    @SerializedName("msg")
    private String message;
    private DataBean data;

    @Data
    public static class DataBean {
        @SerializedName("MASTERID")
        private int masterID;
        @SerializedName("ANCHOR_NICK_NAME")
        private String masterUsername;
        @SerializedName("ROOMID")
        private int roomID;
        @SerializedName("_status")
        private String living;
        @SerializedName("LIVE_STATUS")
        private String liveStatus;
        @SerializedName("AREAID")
        private int areaID;
        @SerializedName("ROOMTITLE")
        private String roomTitle;
        @SerializedName("COVER")
        private String coverImageURL;
        @SerializedName("LIVE_TIMELINE")
        private int liveTimelineMSecond;
        @SerializedName("FANS_COUNT")
        private int masterFansCount;
        @SerializedName("RCOST")
        private int liveScore;
        @SerializedName("GITF_TOP")
        private List<GiftTopBean> giftTop;

        public boolean isLiving() {
            return living.equals("on");
        }

        @Data
        public static class MedalBean {
            private int level;
            private String medal_name;
            private String anchorName;
            private int roomid;
        }
        @Data
        public static class GiftTopBean {
            private int uid;
            @SerializedName("uname")
            private String username;
            private int coin;
            @SerializedName("guard_level")
            private int guardLevel;
            @SerializedName("isSelf")
            private int self;
        }
    }
}
