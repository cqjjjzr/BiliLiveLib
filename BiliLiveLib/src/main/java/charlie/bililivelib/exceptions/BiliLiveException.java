package charlie.bililivelib.exceptions;

/**
 * 所有本Library中异常的根类。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class BiliLiveException extends Exception {
    public BiliLiveException() {
        super();
    }

    public BiliLiveException(String message) {
        super(message);
    }

    public BiliLiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
