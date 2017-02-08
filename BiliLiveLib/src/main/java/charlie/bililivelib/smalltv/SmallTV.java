package charlie.bililivelib.smalltv;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 储存从弹幕服务器发回的小电视数据结构。
 *
 * @author Charlie Jiang
 * @since rv1
 */
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
}
