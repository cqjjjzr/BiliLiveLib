package charlie.bililivelib;

import charlie.bililivelib.i18n.I18n;

public class BiliLiveException extends Exception {
    public BiliLiveException(String message) {
        super(message);
    }

    public static BiliLiveException createCausedException(String message, Exception ex) {
        BiliLiveException exception = new BiliLiveException(message);
        exception.initCause(ex);
        return exception;
    }

    public static BiliLiveException createHttpError(String message, int status) {
        return new BiliLiveException(message + I18n.format("exception.bttp_error", status));
    }
}
