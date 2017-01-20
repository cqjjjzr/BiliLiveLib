package charlie.bililivelib.exceptions;

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
