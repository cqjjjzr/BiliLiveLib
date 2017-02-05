package charlie.bililivelib.smalltv;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.List;

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
        /**
         * lastid : 0
         * join : [{"id":14610,"dtime":85}]
         * unjoin : []
         */

        @SerializedName("lastid")
        private int lastSmallTVID;
        private List<SmallTVShortInfo> join;
        private List<SmallTVShortInfo> unjoin;
    }

    @Getter
    public static class SmallTVShortInfo {
        /**
         * id : 14610
         * dtime : 85
         */

        @SerializedName("id")
        private int smallTVID;
        @SerializedName("dtime")
        private int countDown;
    }
}
