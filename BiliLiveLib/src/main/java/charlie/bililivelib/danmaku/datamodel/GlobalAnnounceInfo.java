package charlie.bililivelib.danmaku.datamodel;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class GlobalAnnounceInfo {
    @SerializedName("msg")
    private String message;
    private int rep;
    @SerializedName("url")
    private String URL;
}
