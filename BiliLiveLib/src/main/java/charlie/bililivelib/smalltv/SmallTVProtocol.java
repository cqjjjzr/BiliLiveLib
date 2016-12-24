package charlie.bililivelib.smalltv;

import charlie.bililivelib.Globals;
import charlie.bililivelib.datamodel.SmallTV;
import charlie.bililivelib.net.HttpHelper;
import com.google.gson.JsonObject;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;

import java.io.IOException;

public class SmallTVProtocol {
    public static final String GET_CURRENT_STV_GET = "/SmallTV/index?roomid=";
    public static final int ERROR_STV_UNAVAILABLE = -400;

    private final HttpHelper httpHelper;
    private final HttpHost BILI_ROOT;

    public SmallTVProtocol() {
        httpHelper = Globals.get().getHttpHelper();
        BILI_ROOT = Globals.get().getBiliLiveRoot();
    }

    /**
     * Returns the small TV that current drawing.
     * <p>
     *     WARNING: DON'T CALL THIS METHOD TO LISTEN SMALL TVs.
     *     USE DanmakuReceiver and GlobalGiftDispatcher to listen small TV lottery via danmaku server.
     * </p>
     * @param roomID Drawing roomID
     * @return The small TV that current drawing. Null if no small TVs is drawing.
     */
    public SmallTV getCurrentSmallTV(int roomID) {
        try {
            HttpResponse response = httpHelper.createGetResponse(BILI_ROOT, generateSmallTVRequest(roomID));
            String jsonString = HttpHelper.responseToString(response);

            JsonObject rootObject = Globals.get().getGson().fromJson(jsonString, JsonObject.class);
            //if (!isSmallTVAvailable(rootObject)) return null;

            System.out.println(rootObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isSmallTVAvailable(JsonObject rootObject) {
        return rootObject.get("code").getAsInt() != ERROR_STV_UNAVAILABLE;
    }

    private String generateSmallTVRequest(int roomID) {
        //http://live.bilibili.com/SmallTV/index?roomid=14026
        return GET_CURRENT_STV_GET + roomID;
    }
}
