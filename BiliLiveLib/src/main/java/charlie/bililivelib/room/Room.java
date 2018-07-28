package charlie.bililivelib.room;

import charlie.bililivelib.Globals;
import charlie.bililivelib.I18n;
import charlie.bililivelib.danmaku.datamodel.UserGuardLevel;
import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.exceptions.NetworkException;
import charlie.bililivelib.exceptions.RoomIDNotFoundException;
import charlie.bililivelib.internalutil.net.HttpHelper;
import charlie.bililivelib.room.datamodel.RoomInfoResponseJson;
import charlie.bililivelib.user.Session;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static charlie.bililivelib.I18n.getString;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * 存放直播间的信息。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Getter
@ToString
public class Room {
    private static final int RESPONSE_SUCCESS = 0;
    private static final int RESPONSE_ROOM_NOT_FOUND = -400;

    private static final String LIVE_ADDRESSES_MF_GET = "/api/playurl?cid={0,number,###}&player=1&quality=0";
    private static final String REAL_ROOMID_GET = "/";
    private static final String LIVE_GET_INFO_GET = "/live/getInfo?roomid=";

    private static final Pattern REAL_ROOMID_PATTERN = Pattern.compile("(?<=var ROOMID = )(\\d+)(?=;)");

    private int roomID;
    private String roomTitle;
    private Image coverImage;
    private Area area;

    private String masterUsername;
    private int masterUID;
    private int masterFansCount;

    private boolean living;
    private RoomStatus status;

    private long liveTimelineMilliSecond;
    private long roomScore;

    @Getter(AccessLevel.PRIVATE)
    private Session session;

    /**
     * 对于给定房间号和会话，创建并填充一个直播间信息对象。
     *
     * @param roomID  直播间号（可以是三位短直播间号）
     * @param session 会话
     * @throws NetworkException        在发生网络问题时抛出
     * @throws RoomIDNotFoundException 在房间号未找到时抛出
     */
    public Room(int roomID, @NotNull Session session) throws BiliLiveException {
        if (roomID < 0) throw new IllegalArgumentException("Room id < 0");
        this.roomID = roomID;
        this.session = session;
        fillRealRoomID();
        fillRoomInfo();
    }

    private void fillRealRoomID() throws BiliLiveException {
        roomID = getRealRoomID(roomID);
    }

    @Contract(pure = true)
    private static String getRealRoomIDRequestURL(int originalRoomID) {
        return REAL_ROOMID_GET + originalRoomID;
    }

    private void fillRoomInfo() throws BiliLiveException {
        try {
            HttpResponse response = session.getHttpHelper().createGetBiliLiveHost(getRoomInfoRequestURL(roomID));
            int statusCode = HttpHelper.getStatusCode(response);

            if (statusCode == HTTP_OK) {
                String jsonString = HttpHelper.responseToString(response);
                fromJson(jsonString);
                return;
            }
            throw NetworkException.createHttpError(getString("exception.fill_room"), statusCode);
        } catch (IOException ex) {
            throw new NetworkException(getString("exception.fill_room"), ex);
        }
    }

    @Contract(pure = true)
    private String getRoomInfoRequestURL(int roomID) {
        return LIVE_GET_INFO_GET + roomID;
    }

    @NotNull
    private String generateInvalidLiveInfoMessage(@NotNull RoomInfoResponseJson json) {
        return I18n.format("exception.fill_room_invalid", json.getCode(), json.getMessage());
    }

    public void fromJson(@NotNull String jsonString) throws BiliLiveException {
        RoomInfoResponseJson jsonObject = Globals.get().gson()
                .fromJson(jsonString, RoomInfoResponseJson.class);

        if (jsonObject.getCode() == RESPONSE_ROOM_NOT_FOUND) {
            throw new RoomIDNotFoundException(generateInvalidLiveInfoMessage(jsonObject));
        } else if (jsonObject.getCode() != RESPONSE_SUCCESS) {
            throw new BiliLiveException(generateInvalidLiveInfoMessage(jsonObject));
        }

        RoomInfoResponseJson.DataBean data = jsonObject.getData();

        roomTitle =       data.getRoomTitle();
        status =          RoomStatus.forName(data.getLiveStatus());
        living =          data.isLiving();
        area =            Area.forID(data.getAreaID());
        masterUsername =  data.getMasterUsername();
        masterUID =       data.getMasterID();
        masterFansCount = data.getMasterFansCount();
        roomScore =       data.getLiveScore();
        liveTimelineMilliSecond = data.getLiveTimelineMSecond();
        coverImage = downloadImage(data.getCoverImageURL());
    }

    private Image downloadImage(@NotNull String url) throws NetworkException {
        try {
            HttpResponse response = session.getHttpHelper().createGetBiliLiveHost(url);
            Image image = ImageIO.read(response.getEntity().getContent());
            EntityUtils.consume(response.getEntity());
            return image;
        } catch (IOException ex) {
            throw new NetworkException(getString("exception.fill_room_invalid"), ex);
        }
    }

    public LiveAddresses fetchLiveAddresses() throws BiliLiveException {
        try {
            HttpResponse response = session.getHttpHelper().createGetBiliLiveHost(getLiveAddressesRequestURL(roomID));
            int statusCode = HttpHelper.getStatusCode(response);

            if (statusCode == HTTP_OK) {
                String xmlString = HttpHelper.responseToString(response);
                return LiveAddresses.fromXMLString(xmlString);
            }
            throw NetworkException.createHttpError(getString("exception.live_addresses"), statusCode);
        } catch (IOException ex) {
            throw new NetworkException(getString("exception.live_addresses"), ex);
        }
    }

    @NotNull
    private String getLiveAddressesRequestURL(int roomID) {
        return MessageFormat.format(LIVE_ADDRESSES_MF_GET, roomID);
    }

    /**
     * 标识直播间的直播状态。
     */
    public enum RoomStatus {
        PREPARING, LIVE, ROUND;

        public static RoomStatus forName(String name) {
            for (RoomStatus roomStatus : RoomStatus.values()) {
                if (roomStatus.name().equals(name)) return roomStatus;
            }
            return PREPARING;
        }

        public String getDisplayName() {
            return I18n.getString("room.status_" + this.name().toLowerCase());
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    /**
     * 标识直播间的分区。
     */
    public enum Area {
        PHONE_LIVE(11),
        SINGER_DANCER(10),
        PAINTING(9),
        OTAKU_CULTURE(2),
        GAME_SINGLE(1),
        GAME_ONLINE(3),
        E_SPORT(4),
        GAME_MOBILE(12),
        THEATER(7),
        PLEASE_CHOOSE(-1);

        @Getter
        private int areaID;

        Area(int areaID) {
            this.areaID = areaID;
        }

        public static Area forID(int areaID) {
            for (Area area : Area.values()) {
                if(area.areaID == areaID) return area;
            }
            return PHONE_LIVE;
        }

        public String getDisplayName() {
            return I18n.getString("room.area_" + this.name().toLowerCase());
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    /**
     * 用于存放直播间的送礼排名。
     */
    @Getter
    public static class GiftRankUser {
        private int uid;
        private UserGuardLevel guardLevel;
        private boolean self;
    }

    public static int getRealRoomID(int roomID) throws NetworkException, RoomIDNotFoundException {
        if (roomID >= 10000) return roomID; // Only need to get real room id when room id is 3/4-digit short id.
        try {
            HttpHelper h = new HttpHelper();
            h.init("");
            HttpResponse response = h.createGetBiliLiveHost(getRealRoomIDRequestURL(roomID));
            int statusCode = HttpHelper.getStatusCode(response);

            if (statusCode == HTTP_OK) {
                String htmlString = HttpHelper.responseToString(response);
                Matcher matcher = REAL_ROOMID_PATTERN.matcher(htmlString);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group());
                } else {
                    throw new RoomIDNotFoundException(I18n.format("exception.roomid_not_found", roomID));
                }
            } else if (statusCode == HTTP_NOT_FOUND) { // NOT FOUND means invalid room id.
                throw new RoomIDNotFoundException(I18n.format("exception.roomid_not_found", roomID));
            } else {
                throw NetworkException.createHttpError(getString("exception.roomid"), statusCode);
            }
        } catch (IOException ex) {
            throw new NetworkException(getString("exception.roomid"), ex);
        }
    }
}
