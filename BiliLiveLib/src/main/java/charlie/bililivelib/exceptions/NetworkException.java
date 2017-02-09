package charlie.bililivelib.exceptions;

import charlie.bililivelib.I18n;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;

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

    /**
     * 对于给定信息和Http错误，创建一个NetworkException。
     *
     * @param message 信息
     * @param status  Http状态码
     * @return NetworkException
     */
    @Contract("_, _ -> !null")
    public static NetworkException createHttpError(
            @Nls String message,
            int status) {
        return new NetworkException(message + I18n.format("exception.http_error", status));
    }
}
