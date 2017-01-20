package charlie.bililivelib.exceptions;

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
