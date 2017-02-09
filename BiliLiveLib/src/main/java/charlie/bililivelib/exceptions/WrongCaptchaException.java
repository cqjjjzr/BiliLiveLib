package charlie.bililivelib.exceptions;

/**
 * 标志验证码输入错误。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class WrongCaptchaException extends BiliLiveException {
    public WrongCaptchaException() {
    }

    public WrongCaptchaException(String message) {
        super(message);
    }

    public WrongCaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}
