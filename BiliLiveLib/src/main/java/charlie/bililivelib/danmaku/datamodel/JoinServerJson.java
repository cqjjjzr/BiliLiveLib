package charlie.bililivelib.danmaku.datamodel;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinServerJson {
    @SerializedName("roomid")
    private int roomID;
    @SerializedName("uid")
    private long userID;
}
