package charlie.bililivelib.danmaku.datamodel;

import charlie.bililivelib.datamodel.Medal;
import charlie.bililivelib.datamodel.User;
import charlie.bililivelib.datamodel.UserGuardLevel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Danmaku {
    /*
    JSON FORMAT:
{
    "info":[
        [
            UNKNOWN,
            UNKNOWN,
            UNKNOWN,
            UNKNOWN,
            UNKNOWN,
            "UNKNOWN",
            UNKNOWN,
            "USER_ID_CRC32",
            UNKNOWN
        ],
        "DANMAKU_CONTENT",
        [
            USER_ID,
            "USER_NAME",
            IS_GUARD,
            IS_VIP,
            UNKNOWN,
            UNKNOWN,
            UNKNOWN
        ],
        [
            MEDAL_LEVEL,
            "MEDAL_NAME",
            "MEDAL_MASTER_MAME",
            MEDAL_MASTER_ROOM,
            MEDAL(UNKNOWN)
        ],
        [
            USER_LEVEL,
            UNKNOWN,
            USER_EXP,
            USER_LEVEL_RANK
        ],
        [
            TITLE(头衔),
            TITLE
        ],
        UNKNOWN,
        UNKNOWN
    ],
    "cmd":"DANMU_MSG"
}
     */
    private static final int MEDAL_ROOT_INDEX = 3;
    private static final int UID_CRC32_ROOT_INDEX = 0;
    private static final int USER_INFO_ROOT_INDEX = 2;
    private static final int USER_LEVEL_INFO_ROOT_INDEX = 4;
    private static final int TITLE_INFO_ROOT_INDEX = 5;
    private static final int CONTENT_INDEX = 1;

    private User user;
    private String content;

    public Danmaku(JsonObject rootElement) {
        JsonArray infoElement = rootElement.get("info").getAsJsonArray();

        Medal medal = buildMedal(infoElement.get(MEDAL_ROOT_INDEX).getAsJsonArray());
        user = buildUser(infoElement, medal);

        content = infoElement.get(CONTENT_INDEX).getAsString();
    }

    private static final int UID_CRC32_ARRAY_INDEX = 5;
    private static final int USER_NAME_INDEX = 1;
    private static final int UID_INDEX = 0;
    private static final int GUARD_INDEX = 2;
    private static final int VIP_INDEX = 3;
    private static final int USER_LEVEL_INDEX = 0;
    private static final int USER_EXP_INDEX = 2;
    private static final int USER_LEVEL_RANK_INDEX = 3;
    private static final int USER_TITLE_INDEX = 0;
    private User buildUser(JsonArray array, Medal medal) {
        User user = new User();
        user.setUidCRC32(array.get(UID_CRC32_ROOT_INDEX).getAsJsonArray()
                                .get(UID_CRC32_ARRAY_INDEX).getAsInt());

        JsonArray userInfoArray = array.get(USER_INFO_ROOT_INDEX).getAsJsonArray();
        user.setName(userInfoArray.get(USER_NAME_INDEX).getAsString());
        user.setUid(userInfoArray.get(UID_INDEX).getAsInt());
        user.setGuardLevel(UserGuardLevel.fromLevel(userInfoArray.get(GUARD_INDEX).getAsInt()));
        user.setVip(isVipFromInt(userInfoArray.get(VIP_INDEX).getAsInt()));

        JsonArray levelInfoArray = array.get(USER_LEVEL_INFO_ROOT_INDEX).getAsJsonArray();
        user.setLevel(levelInfoArray.get(USER_LEVEL_INDEX).getAsInt());
        user.setExp(levelInfoArray.get(USER_EXP_INDEX).getAsInt());
        user.setLevelRank(levelRankFromString(levelInfoArray.get(USER_LEVEL_RANK_INDEX).getAsString()));

        user.setTitle(titleFromArray(array.get(TITLE_INFO_ROOT_INDEX).getAsJsonArray()));
        user.setMedal(medal);
        return user;
    }

    private String titleFromArray(JsonArray array) {
        if (array.size() < 1)
            return null;
        return array.get(USER_TITLE_INDEX).getAsString();
    }

    private int levelRankFromString(String string) {
        if (string.equals(">50000"))
            return Integer.MAX_VALUE;
        return Integer.parseInt(string);
    }

    private boolean isVipFromInt(int vip) {
        return vip == 1;
    }

    private static final int MEDAL_LEVEL_INDEX = 0;
    private static final int MEDAL_NAME_INDEX = 1;
    private static final int MEDAL_MASTER_NAME_INDEX = 2;
    private static final int MEDAL_MASTER_ROOM_INDEX = 3;
    private Medal buildMedal(JsonArray array) {
        if (array.size() < 4) return null;
        Medal medal = new Medal();
        medal.setName(array.get(MEDAL_NAME_INDEX).getAsString());
        medal.setMasterName(array.get(MEDAL_MASTER_NAME_INDEX).getAsString());
        medal.setLevel((short) array.get(MEDAL_LEVEL_INDEX).getAsInt());
        medal.setMasterRoomID(array.get(MEDAL_MASTER_ROOM_INDEX).getAsInt());
        return medal;
    }
}
