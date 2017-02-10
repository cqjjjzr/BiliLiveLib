package charlie.bililivelib.danmaku.datamodel;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用来封装直播间状态的改变信息。
 *
 * @author Charlie Jiang
 * @since rv1
 */
@Data
@AllArgsConstructor
public class StartStopInfo {
    private int roomID;
    private boolean living;
}
