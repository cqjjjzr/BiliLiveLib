package charlie.bililivelib.exceptions;

/**
 * 标志发生了网络问题的异常。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class NetworkException extends BiliLiveException {
    public NetworkException() {
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
