package charlie.bililivelib.smalltv;

import charlie.bililivelib.I18n;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 储存小电视抽奖结果的数据结构。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Data
public class SmallTVReward {
    private static final int STATUS_MISS = 1;

    /*
     * code : 0
     * msg : OK
     * data : {"fname":"奶酪酪酪酪","sname":"南傾挽风","reward":{"id":7,"num":2},"win":0,"status":0}
     */

    private int code;
    @SerializedName("msg")
    private String message;
    @Getter(AccessLevel.PRIVATE)
    private DataBean data;

    public boolean isMiss() {
        return data.status == STATUS_MISS;
    }

    public String getSenderUsername() {
        return data.senderUsername;
    }

    public String getFirstPrizeUsername() {
        return data.prizeUsername;
    }

    @Nullable
    public Reward getReward() {
        return data.reward;
    }

    public boolean isWon() {
        return data.win == 0;
    }

    public int getStatus() {
        return data.status;
    }

    public boolean isStillDrawing() {
        return data.status == 2;
    }

    private static class DataBean {
        /*
         * fname : 奶酪酪酪酪
         * sname : 南傾挽风
         * reward : {"id":7,"num":2}
         * win : 0
         * status : 0
         */

        @SerializedName("fname")
        private String senderUsername;
        @SerializedName("sname")
        private String prizeUsername;
        private Reward reward;
        private int win;
        private int status;
    }

    /**
     * 储存一个小电视抽奖奖励。
     */
    public static class Reward {
        /*
         * id : 7
         * num : 2
         */

        @SerializedName("id")
        private int id;
        @SerializedName("num")
        @Getter
        private int count;

        @NotNull
        public Kind getKind() {
            return Kind.forID(id);
        }

        public enum Kind {
            SMALL_TV(1),
            BLUE_WHITE_PANTS(2),
            B_KELA(3),
            NYA_NIANG(4),
            BENTO(5),
            SLIVER(6),
            HOT_STRIP(7),
            UNKNOWN(-1);
            @Getter
            private int id;

            Kind(int id) {
                this.id = id;
            }

            @NotNull
            @Contract(pure = true)
            public static Kind forID(int id) {
                for (Kind reward : Kind.values()) {
                    if (reward.getId() == id) return reward;
                }
                return UNKNOWN;
            }

            public String getDisplayName() {
                return I18n.getString("smalltv.reward." + name().toLowerCase());
            }
        }
    }
}
