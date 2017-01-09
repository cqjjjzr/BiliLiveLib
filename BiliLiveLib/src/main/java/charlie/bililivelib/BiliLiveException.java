package charlie.bililivelib;

import charlie.bililivelib.util.I18n;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BiliLiveException extends Exception {
    public BiliLiveException(String message) {
        super(message);
    }

    public static BiliLiveException createCausedException(@Nls String message, @NotNull Exception ex) {
        BiliLiveException exception = new BiliLiveException(message);
        exception.initCause(ex);
        StackTraceElement[] elementsOriginal = exception.getStackTrace();
        exception.setStackTrace(Arrays.copyOfRange(elementsOriginal, 1, elementsOriginal.length));
        return exception;
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
