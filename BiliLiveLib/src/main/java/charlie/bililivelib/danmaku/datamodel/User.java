package charlie.bililivelib.danmaku.datamodel;

import lombok.Data;

/**
 * 标志一个发送弹幕的用户。
 *
 * @author Charlie Jiang
 * @since rv1
 */
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
