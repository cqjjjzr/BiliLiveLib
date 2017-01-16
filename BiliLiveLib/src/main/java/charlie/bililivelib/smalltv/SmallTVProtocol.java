package charlie.bililivelib.smalltv;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.session.Session;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

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

        SmallTVRoom room = httpHelper.getBiliLiveJSON(generateSmallTVRequest(roomID), SmallTVRoom.class,
                "exception.smalltv_room");
        if (!isSmallTVAvailable(room)) return null;
        return room;
    }

    public void joinLottery(SmallTV smallTV) throws BiliLiveException {
        JsonObject rootObject = httpHelper.getBiliLiveJSON(
                generateJoinSmallTVRequest(smallTV.getRealRoomID(), smallTV.getSmallTVID()),
                JsonObject.class,
                "exception.smalltv_join");

        if (!isJoinSuccessfullyAndNotStarted(rootObject)) throw new BiliLiveException(getErrorMessage(rootObject));
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

    private boolean isSmallTVAvailable(SmallTVRoom room) {
        return room.getCode() == SUCCESS;
    }

    @Contract(pure = true)
    private String generateSmallTVRequest(int roomID) {
        // http://live.bilibili.com/SmallTV/index?roomid=14026
        return GET_CURRENT_STV_GET + roomID;
    }

    public SmallTVReward getReward(int smallTVID) throws BiliLiveException {
        if (smallTVID < 1) throw new IllegalArgumentException("SmallTVID < 1");

        return httpHelper.getBiliLiveJSON(generateGetRewardRequest(smallTVID), SmallTVReward.class,
                "exception.smalltv_reward");
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
