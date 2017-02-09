package charlie.bililivelib.exceptions;

import charlie.bililivelib.room.Room;

/**
 * 标示从服务器返回的直播流地址无效。一般不会抛出，可能是传递了错误的直播间号。
 *
 * @author Charlie Jiang
 * @see Room
 * @since rv1
 */
public class InvalidLiveAddressException extends BiliLiveException {
    public InvalidLiveAddressException() {
    }

    public InvalidLiveAddressException(String message) {
        super(message);
    }

    public InvalidLiveAddressException(String message, Throwable cause) {
        super(message, cause);
    }
}
