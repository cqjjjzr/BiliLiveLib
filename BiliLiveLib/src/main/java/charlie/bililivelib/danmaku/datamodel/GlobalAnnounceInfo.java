package charlie.bililivelib.danmaku.datamodel;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 用于存放全屏公告信息。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Data
public class GlobalAnnounceInfo {
    @SerializedName("msg")
    private String message;
    private int rep;
    @SerializedName("url")
    private String URL;
}
