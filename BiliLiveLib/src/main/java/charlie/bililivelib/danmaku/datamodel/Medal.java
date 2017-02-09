package charlie.bililivelib.danmaku.datamodel;

import lombok.Data;

/**
 * 标志一个主播勋章。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Data
public class Medal {
    private short level;
    private String name;
    private String masterName;
    private int masterRoomID;
}