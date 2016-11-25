package charlie.bililivelib.protocol.datamodel;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ResponseJson {
    private int code;
    @SerializedName("msg")
    private String message;
}
