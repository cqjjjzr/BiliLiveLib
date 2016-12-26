package charlie.bililivelib.smalltv;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.Globals;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.session.Session;
import com.google.gson.JsonObject;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;

import java.io.IOException;

public class SmallTVProtocol {
    public static final int SUCCESS = 0;

    private final HttpHelper httpHelper;
    private final HttpHost BILI_ROOT;

    public SmallTVProtocol(Session session) {
        httpHelper = session.getHttpHelper();
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
    public SmallTVRoom getCurrentSmallTV(int roomID) throws BiliLiveException {
        try {
            HttpResponse response = httpHelper.createGetResponse(BILI_ROOT, generateSmallTVRequest(roomID));
            String jsonString = HttpHelper.responseToString(response);

            JsonObject rootObject = Globals.get().getGson().fromJson(jsonString, JsonObject.class);
            if (!isSmallTVAvailable(rootObject)) return null;

            return Globals.get().getGson().fromJson(rootObject, SmallTVRoom.class);
        } catch (IOException e) {
            throw BiliLiveException.createCausedException("", e);
        }
    }

    public void joinLottery(SmallTVRoom smallTV) throws BiliLiveException {
        try {
            HttpResponse response = httpHelper.createGetResponse(BILI_ROOT, generateJoinSmallTVRequest(
                    smallTV.getRealRoomID(), smallTV.getSmallTVID()
            ));
            String jsonString = HttpHelper.responseToString(response);

            JsonObject rootObject = Globals.get().getGson().fromJson(jsonString, JsonObject.class);
            if (!isJoinSuccessfullyAndNotStarted(rootObject)) throw new BiliLiveException(getErrorMessage(rootObject));

            updateSmallTV(smallTV);
        } catch (IOException e) {
            throw BiliLiveException.createCausedException("", e);
        }
    }

    public void updateSmallTV(SmallTVRoom smallTV) throws BiliLiveException {
        SmallTVRoom tempSmallTV = getCurrentSmallTV(smallTV.getRealRoomID());
        if (tempSmallTV == null) return;
        smallTV.setData(tempSmallTV.getData());
        smallTV.setMessage(tempSmallTV.getMessage());
    }

    private String getErrorMessage(JsonObject rootObject) {
        return rootObject.get("msg").getAsString();
    }

    private static final int JOINED_AND_NOT_STARTED = 1;
    private boolean isJoinSuccessfullyAndNotStarted(JsonObject rootObject) {
        return rootObject.get("code").getAsInt() == SUCCESS &&
                rootObject.get("data").getAsJsonObject().get("code").getAsInt() == JOINED_AND_NOT_STARTED;
    }

    private static final String JOIN_STV_GET_PT1 = "/SmallTVRoom/join?roomid=";
    private static final String JOIN_STV_GET_PT2 = "&id=";
    private String generateJoinSmallTVRequest(int realRoomID, int smallTVID) {
        //http://live.bilibili.com/SmallTV/join?roomid=14026&id=7116
        return JOIN_STV_GET_PT1 + realRoomID + JOIN_STV_GET_PT2 + smallTVID;
    }

    private boolean isSmallTVAvailable(JsonObject rootObject) {
        return rootObject.get("code").getAsInt() == SUCCESS;
    }

    private static final String GET_CURRENT_STV_GET = "/SmallTVRoom/index?roomid=";
    private String generateSmallTVRequest(int roomID) {
        //http://live.bilibili.com/SmallTV/index?roomid=14026
        return GET_CURRENT_STV_GET + roomID;
    }
}
