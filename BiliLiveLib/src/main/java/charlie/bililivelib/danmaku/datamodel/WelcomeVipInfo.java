package charlie.bililivelib.danmaku.datamodel;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WelcomeVipInfo {
    private String username;
    private boolean admin;

    public WelcomeVipInfo(JsonObject rootObject) {
        JsonObject dataObject = rootObject.get("data").getAsJsonObject();

        username = dataObject.get("uname").getAsString();
        admin = dataObject.get("isadmin").getAsBoolean();
    }
}
