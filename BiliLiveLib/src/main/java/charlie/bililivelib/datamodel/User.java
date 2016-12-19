package charlie.bililivelib.datamodel;

import lombok.Data;

@Data
public class User {
    private String name;
    private int uid;
    private int uidCRC32;
    private UserGuardLevel guardLevel;
    private Medal medal;
    private boolean vip;

    private int level;
    private int exp;
    private int levelRank;
    private String title;
}
