package charlie.bililivelib.exceptions;

import charlie.bililivelib.util.I18n;
import org.jetbrains.annotations.Nls;

import java.util.Arrays;

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

    public static BiliLiveException createHttpError(
            @Nls String message,
            int status) {
        BiliLiveException exception = new BiliLiveException(message + I18n.format("exception.http_error", status));
        StackTraceElement[] elementsOriginal = exception.getStackTrace();
        exception.setStackTrace(Arrays.copyOfRange(elementsOriginal, 1, elementsOriginal.length));
        return exception;
    }
}
