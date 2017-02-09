package charlie.bililivelib.exceptions;

/**
 * 标志未登录时访问了需要登录的操作。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class NotLoggedInException extends BiliLiveException {
    public NotLoggedInException() {
        super();
    }

    public NotLoggedInException(String message) {
        super(message);
    }

    public NotLoggedInException(String message, Throwable cause) {
        super(message, cause);
    }
}
