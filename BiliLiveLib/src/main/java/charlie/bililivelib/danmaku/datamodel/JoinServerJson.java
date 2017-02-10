package charlie.bililivelib.danmaku.datamodel;

import charlie.bililivelib.Globals;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用于封装加入服务器的请求信息。仅内部使用。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Data
@AllArgsConstructor
public class JoinServerJson {
    @SerializedName("roomid")
    private int roomID;
    @SerializedName("uid")
    private long userID;

    private String generateJSON() {
        return Globals.get().gson().toJson(this);
    }
}
