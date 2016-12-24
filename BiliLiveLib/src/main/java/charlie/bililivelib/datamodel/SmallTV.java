package charlie.bililivelib.datamodel;

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
    private String smallTVID;
}
