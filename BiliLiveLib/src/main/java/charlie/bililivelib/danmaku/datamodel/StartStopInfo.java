package charlie.bililivelib.danmaku.datamodel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartStopInfo {
    private int roomID;
    private boolean living;
}
