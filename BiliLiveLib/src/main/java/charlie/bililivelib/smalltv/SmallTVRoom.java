package charlie.bililivelib.smalltv;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * 保存一个房间的小电视状态。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Data
public class SmallTVRoom {
    /**
     * code : 0
     * msg : OK
     * data : {"lastid":0,"join":[{"id":14610,"dtime":85}],"unjoin":[]}
     */

    private int code;
    @SerializedName("msg")
    private String message;
    @Getter(AccessLevel.PRIVATE)
    private DataBean data;

    public List<SmallTVShortInfo> getJoinedSmallTVs() {
        return data.join;
    }

    public List<SmallTVShortInfo> getNotJoinedSmallTVs() {
        return data.unjoin;
    }

    private static class DataBean {
        /*
         * lastid : 0
         * join : [{"id":14610,"dtime":85}]
         * unjoin : []
         */

        @SerializedName("lastid")
        private int lastSmallTVID;
        private List<SmallTVShortInfo> join;
        private List<SmallTVShortInfo> unjoin;
    }

    /**
     * 用于保存一个小电视的粗略数据。
     * @author Charlie Jiang
     * @since rv1
     */
    @Getter
    public static class SmallTVShortInfo {
        /**
         * id : 14610
         * dtime : 85
         */

        @SerializedName("id")
        private int smallTVID;
        @SerializedName("dtime")
        private int countDownSecond;
    }
}
