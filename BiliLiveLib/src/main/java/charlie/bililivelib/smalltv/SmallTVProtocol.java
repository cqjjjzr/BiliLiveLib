package charlie.bililivelib.smalltv;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.Globals;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.session.Session;
import charlie.bililivelib.util.I18n;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static charlie.bililivelib.util.I18n.format;

public class SmallTVProtocol {
    public static final int SUCCESS = 0;
    public static final int STATUS_NO_REWARD = 1;
    private static final int JOINED_AND_NOT_STARTED = 1;
    private static final String JOIN_STV_GET_PT1 = "/SmallTV/join?roomid=";
    private static final String JOIN_STV_GET_PT2 = "&id=";
    private static final String GET_CURRENT_STV_GET = "/SmallT/index?roomid=";
    private static final String GET_REWARD_GET = "/SmallTV/getReward?id=";
    private final HttpHelper httpHelper;

    public SmallTVProtocol(Session session) {
        httpHelper = session.getHttpHelper();
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
    @Nullable
    public SmallTVRoom getSmallTVRoom(int roomID) throws BiliLiveException {
        if (roomID < 1) throw new IllegalArgumentException("roomID < 0");

        try {
            HttpResponse response = httpHelper.createGetBiliLiveHost(generateSmallTVRequest(roomID));
            String jsonString = HttpHelper.responseToString(response);

            JsonObject rootObject = Globals.get().getGson().fromJson(jsonString, JsonObject.class);
            if (!isSmallTVAvailable(rootObject)) return null;

            return Globals.get().getGson().fromJson(rootObject, SmallTVRoom.class);
        } catch (IOException e) {
            throw new BiliLiveException(format("exception.smalltv_room", roomID), e);
        }
    }

    public void joinLottery(SmallTV smallTV) throws BiliLiveException {
        try {
            HttpResponse response = httpHelper.createGetBiliLiveHost(generateJoinSmallTVRequest(
                    smallTV.getRealRoomID(), smallTV.getSmallTVID()
            ));
            String jsonString = HttpHelper.responseToString(response);

            JsonObject rootObject = Globals.get().getGson().fromJson(jsonString, JsonObject.class);
            if (!isJoinSuccessfullyAndNotStarted(rootObject)) throw new BiliLiveException(getErrorMessage(rootObject));
        } catch (IOException e) {
            throw new BiliLiveException(format("exception.smalltv_join", smallTV.getRealRoomID()), e);
        }
    }

    private String getErrorMessage(JsonObject rootObject) {
        return rootObject.get("msg").getAsString();
    }

    private boolean isJoinSuccessfullyAndNotStarted(JsonObject rootObject) {
        return rootObject.get("code").getAsInt() == SUCCESS &&
                rootObject.get("data").getAsJsonObject().get("status").getAsInt() == JOINED_AND_NOT_STARTED;
    }

    @Contract(pure = true)
    private String generateJoinSmallTVRequest(int realRoomID, int smallTVID) {
        // http://live.bilibili.com/SmallTV/join?roomid=14026&id=7116
        return JOIN_STV_GET_PT1 + realRoomID + JOIN_STV_GET_PT2 + smallTVID;
    }

    private boolean isSmallTVAvailable(JsonObject rootObject) {
        return rootObject.get("code").getAsInt() == SUCCESS;
    }

    @Contract(pure = true)
    private String generateSmallTVRequest(int roomID) {
        // http://live.bilibili.com/SmallTV/index?roomid=14026
        return GET_CURRENT_STV_GET + roomID;
    }

    public SmallTVReward getReward(int smallTVID) throws BiliLiveException {
        if (smallTVID < 1) throw new IllegalArgumentException("SmallTVID < 1");

        try {
            HttpResponse response = httpHelper.createGetBiliLiveHost(generateGetRewardRequest(smallTVID));
            String jsonString = HttpHelper.responseToString(response);
            JsonObject rootObject = Globals.get().getGson().fromJson(jsonString, JsonObject.class);

            return Globals.get().getGson().fromJson(rootObject, SmallTVReward.class);
        } catch (IOException ex) {
            throw new BiliLiveException(I18n.format("exception.smalltv_reward", smallTVID), ex);
        }
    }

    private boolean isGotReward(JsonObject rootObject) {
        // UNUSED: return empty SmallTVReward.
        return rootObject.get("data").getAsJsonObject()
                .get("status").getAsInt() != STATUS_NO_REWARD;
    }

    private String generateGetRewardRequest(int smallTVID) {
        return GET_REWARD_GET + smallTVID;
    }
}
