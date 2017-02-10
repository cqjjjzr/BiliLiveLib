package charlie.bililivelib.smalltv;

import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.exceptions.NetworkException;
import charlie.bililivelib.exceptions.NotLoggedInException;
import charlie.bililivelib.internalutil.net.HttpHelper;
import charlie.bililivelib.user.Session;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * 用于进行小电视抽奖的协议类。
 *
 * @author Charlie Jiang
 * @see SmallTVReward
 * @see SmallTV
 * @see SmallTVRoom
 * @since rv1
 */
public class SmallTVProtocol {
    public static final int SUCCESS = 0;
    public static final int STATUS_NO_REWARD = 1;
    private static final int JOINED_AND_NOT_STARTED = 1;
    private static final String JOIN_STV_GET_PT1 = "/SmallTV/join?roomid=";
    private static final String JOIN_STV_GET_PT2 = "&id=";
    private static final String GET_CURRENT_STV_GET = "/SmallTV/index?roomid=";
    private static final String GET_REWARD_GET = "/SmallTV/getReward?id=";
    private final HttpHelper httpHelper;

    public SmallTVProtocol(Session session) {
        httpHelper = session.getHttpHelper();
    }

    /**
     * 返回给定房间号的小电视情况。
     * <p>
     *     警告：不要使用这个方法来监听小电视情况。效率极低。
     *     使用 {@link charlie.bililivelib.danmaku.DanmakuReceiver} 和
     *     {@link charlie.bililivelib.danmaku.dispatch.GlobalGiftDispatcher} 监听全局小电视事件。
     * </p>
     * @param roomID 给定的房间号
     * @return 小电视情况，若该房间无小电视抽奖则返回Null
     * @throws NetworkException 发生网络问题时抛出
     */
    @Nullable
    public SmallTVRoom getSmallTVRoom(int roomID) throws BiliLiveException {
        if (roomID < 1) throw new IllegalArgumentException("roomID < 0");

        SmallTVRoom room = httpHelper.getBiliLiveJSON(generateSmallTVRequest(roomID), SmallTVRoom.class,
                "exception.smalltv_room");
        if (!isSmallTVAvailable(room)) return null;
        return room;
    }

    /**
     * 加入给定小电视抽奖。
     * @param smallTV 给定小电视
     * @throws BiliLiveException 在加入失败时抛出
     * @throws NotLoggedInException 未登录时抛出
     * @throws NetworkException 发生网络问题时抛出
     */
    public void joinLottery(SmallTV smallTV) throws BiliLiveException {
        JsonObject rootObject = httpHelper.getBiliLiveJSONAndCheckLogin(
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

    /**
     * 获取给定小电视获取到的奖励。
     * @param smallTVID 给定小电视的ID
     * @return 抽奖结果
     * @throws NotLoggedInException 未登录时抛出
     * @throws NetworkException 发生网络问题时抛出
     * @throws IllegalArgumentException 给定ID{@code < 1}时抛出
     */
    public SmallTVReward getReward(int smallTVID) throws BiliLiveException {
        if (smallTVID < 1) throw new IllegalArgumentException("SmallTVID < 1");

        return httpHelper.getBiliLiveJSONAndCheckLogin(generateGetRewardRequest(smallTVID), SmallTVReward.class,
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
