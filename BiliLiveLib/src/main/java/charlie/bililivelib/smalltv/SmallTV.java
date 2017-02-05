package charlie.bililivelib.smalltv;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class SmallTV {
    @SerializedName("msg")
    private String message;
    @SerializedName("url")
    private String roomURL;
    @SerializedName("roomid")
    private int roomID;
    @SerializedName("real_roomid")
    private int realRoomID;
    @SerializedName("tv_id")
    private int smallTVID;

    /*private DataInfo data;

    @Data
    public class DataInfo {
        @SerializedName("lastid")
        private int lastID;

        @SerializedName("join")
        private List<SmallTVJoinInfo> joinedInfo = null;
        @SerializedName("unjoin")
        private List<SmallTVJoinInfo> notJoinedInfo = null;

        @Data
        public class SmallTVJoinInfo {
            private int id;
            @SerializedName("dtime")
            private int countDownSecond;
        }
    }*/
}
