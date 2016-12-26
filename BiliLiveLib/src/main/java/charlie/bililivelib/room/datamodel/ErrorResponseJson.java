package charlie.bililivelib.room.datamodel;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ErrorResponseJson {
    private int code;
    @SerializedName("msg")
    private String message;
}
