package charlie.bililivelib.smalltv;

import lombok.Getter;

@Getter
public enum SmallTVReward {
    SMALL_TV        ("大号小电视", 1),
    BLUE_WHITE_PANTS("蓝白胖次", 2),
    B_KELA          ("B坷垃", 3),
    NYA_NIANG       ("喵娘", 4),
    BENTO           ("便当", 5),
    SLIVER          ("银瓜子", 6),
    HOT_STRIP       ("辣条", 7);

    private String displayName;
    private int id;

    SmallTVReward(String displayName, int id) {
        this.displayName = displayName;
        this.id = id;
    }

    public static SmallTVReward fromID(int id) {
        for (SmallTVReward reward : SmallTVReward.values()) {
            if (reward.getId() == id) return reward;
        }
        return HOT_STRIP;
    }
}
