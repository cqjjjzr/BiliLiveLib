package charlie.bililivelib.danmaku.datamodel;

import charlie.bililivelib.Globals;
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

    private String generateJSON() {
        return Globals.get().getGson().toJson(this);
    }
}
