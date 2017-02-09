package charlie.bililivelib.exceptions;

import charlie.bililivelib.room.Room;

/**
 * 直播间号未找到。
 *
 * @author Charlie Jiang
 * @see Room
 * @since rv1
 */
public class RoomIDNotFoundException extends BiliLiveException {
    public RoomIDNotFoundException() {
    }

    public RoomIDNotFoundException(String message) {
        super(message);
    }

    public RoomIDNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
