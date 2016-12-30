package charlie.bililivelib.room;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.Globals;
import charlie.bililivelib.datamodel.UserGuardLevel;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.room.datamodel.RoomInfoResponseJson;
import charlie.bililivelib.session.Session;
import charlie.bililivelib.util.I18n;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static charlie.bililivelib.util.I18n.getString;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

@Getter
@ToString
public class Room {
    public static final String LIVE_ADDRESSES_MF_GET = "/api/playurl?cid={0,number,###}&player=1&quality=0";
    private static final int RESPONSE_SUCCESS_CODE = 0;
    private static final Pattern REAL_ROOMID_PATTERN = Pattern.compile("(?<=var ROOMID = )(\\d+)(?=;)");
    private static final String REAL_ROOMID_GET = "/";
    private static final String LIVE_GET_INFO_GET = "/live/getInfo?roomid=";
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

    public Room(int roomID, Session session) throws BiliLiveException {
        this.roomID = roomID;
        this.session = session;
        fillRealRoomID();
        fillRoomInfo();
    }

    private void fillRealRoomID() throws BiliLiveException {
        try {
            HttpResponse response = session.getHttpHelper().createGetBiliLiveHost(getRealRoomIDRequestURL(roomID));
            int statusCode = HttpHelper.getStatusCode(response);

            if (statusCode == HTTP_OK) {
                String htmlString = HttpHelper.responseToString(response);
                parseAndFillRealRoomID(htmlString);
            } else if (statusCode == HTTP_NOT_FOUND) { //NOT FOUND means invalid room id.
                throw new BiliLiveException(I18n.format("exception.roomid_not_found", roomID));
            } else {
                throw BiliLiveException.createHttpError(getString("exception.roomid"), statusCode);
            }
        } catch (IOException ex) {
            throw BiliLiveException.createCausedException(getString("exception.roomid"), ex);
        }
    }

    private String getRealRoomIDRequestURL(int originalRoomID) {
        return REAL_ROOMID_GET + originalRoomID;
    }

    private void parseAndFillRealRoomID(String httpString) throws BiliLiveException {
        Matcher matcher = REAL_ROOMID_PATTERN.matcher(httpString);
        if (matcher.find()) {
            roomID = Integer.parseInt(matcher.group());
        } else {
            throw new BiliLiveException(I18n.format("exception.roomid_not_found", roomID));
        }
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
            throw BiliLiveException.createHttpError(getString("exception.fill_room"), statusCode);
        } catch (IOException ex) {
            throw BiliLiveException.createCausedException(getString("exception.fill_room"), ex);
        }
    }

    private String getRoomInfoRequestURL(int roomID) {
        return LIVE_GET_INFO_GET + roomID;
    }

    private String generateInvalidLiveInfoMessage(RoomInfoResponseJson json) {
        return I18n.format("exception.fill_room_invalid", json.getCode(), json.getMessage());
    }

    public void fromJson(String jsonString) throws BiliLiveException {
        RoomInfoResponseJson jsonObject = Globals.get().getGson()
                .fromJson(jsonString, RoomInfoResponseJson.class);

        if (jsonObject.getCode() != RESPONSE_SUCCESS_CODE) {
            throw new BiliLiveException(generateInvalidLiveInfoMessage(jsonObject));
        }

        RoomInfoResponseJson.DataBean data = jsonObject.getData();

        roomTitle =       data.getRoomTitle();
        status =          RoomStatus.forName(data.getLiveStatus());
        living =          data.isLiving();
        area =            Area.forID(data.getAreaID());
        coverImage =      downloadImage(data.getCoverImageURL());
        masterUsername =  data.getMasterUsername();
        masterUID =       data.getMasterID();
        masterFansCount = data.getMasterFansCount();
        roomScore =       data.getLiveScore();
        liveTimelineMilliSecond = data.getLiveTimelineMSecond();
    }

    private Image downloadImage(String url) throws BiliLiveException {
        try {
            HttpResponse response = session.getHttpHelper().createGetBiliLiveHost(url);
            Image image = ImageIO.read(response.getEntity().getContent());
            EntityUtils.consume(response.getEntity());
            return image;
        } catch (IOException ex) {
            throw BiliLiveException.createCausedException(getString("exception.fill_room_invalid"), ex);
        }
    }

    public LiveAddresses getLiveAddresses() throws BiliLiveException {
        try {
            HttpResponse response = session.getHttpHelper().createGetBiliLiveHost(getLiveAddressesRequestURL(roomID));
            int statusCode = HttpHelper.getStatusCode(response);

            if (statusCode == HTTP_OK) {
                String xmlString = HttpHelper.responseToString(response);
                return LiveAddresses.fromXMLString(xmlString);
            }
            throw BiliLiveException.createHttpError(getString("exception.live_addresses"), statusCode);
        } catch (IOException ex) {
            throw BiliLiveException.createCausedException(getString("exception.live_addresses"), ex);
        }
    }

    private String getLiveAddressesRequestURL(int roomID) {
        return MessageFormat.format(LIVE_ADDRESSES_MF_GET, roomID);
    }

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

    @Getter
    @Setter
    public static class GiftRankUser {
        private int uid;
        private UserGuardLevel guardLevel;
        private boolean self;
    }
}
