package charlie.bililivelib.datamodel;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.Globals;
import charlie.bililivelib.i18n.I18n;
import charlie.bililivelib.protocol.datamodel.RoomInfoResponseJson;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

import static charlie.bililivelib.i18n.I18n.getString;

@Getter
@Setter
@ToString
public class Room {
    private static final int RESPONSE_SUCCESS_CODE = 0;

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

    public void fromJson(String jsonString) throws BiliLiveException {
        RoomInfoResponseJson jsonObject = Globals.get().getGson()
                .fromJson(jsonString, RoomInfoResponseJson.class);

        if (jsonObject.getCode() != RESPONSE_SUCCESS_CODE) {
            throw new BiliLiveException(generateInvalidLiveInfoMessage(jsonObject));
        }

        RoomInfoResponseJson.DataBean data = jsonObject.getData();

        this.setRoomTitle      (data.getRoomTitle());
        this.setStatus         (RoomStatus.forName(data.getLiveStatus()));
        this.setLiving         (data.isLiving());
        this.setArea           (Area.forID(data.getAreaID()));
        this.setCoverImage     (downloadImage(data.getCoverImageURL()));
        this.setMasterUsername (data.getMasterUsername());
        this.setMasterUID      (data.getMasterID());
        this.setMasterFansCount(data.getMasterFansCount());
        this.setRoomScore      (data.getLiveScore());
        this.setLiveTimelineMilliSecond(data.getLiveTimelineMSecond());
    }

    private Image downloadImage(String url) throws BiliLiveException {
        try {
            HttpResponse response = Globals.get().getHttpHelper()
                    .createGetResponse(Globals.get().getBiliLiveRoot(),
                            url);
            Image image = ImageIO.read(response.getEntity().getContent());
            EntityUtils.consume(response.getEntity());
            return image;
        } catch (IOException ex) {
            throw BiliLiveException.createCausedException(getString("exception.fill_room_invalid"), ex);
        }
    }

    private String generateInvalidLiveInfoMessage(RoomInfoResponseJson json) {
        return I18n.format("exception.fill_room_invalid", json.getCode(), json.getMessage());
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
