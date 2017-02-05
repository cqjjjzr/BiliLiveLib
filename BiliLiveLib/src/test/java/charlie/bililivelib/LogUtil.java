package charlie.bililivelib;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;

public class LogUtil {
    private static final String LINE_BREAKING = System.lineSeparator();

    private static Logger logger;

    public static void init() {
        logger = LogManager.getLogger(BiliLiveLib.class);
    }

    public static synchronized void logException(Level level, @NotNull @Nls String message, @NotNull Throwable ex) {
        if (logger == null) return;
        logger.log(level, message);
        StringTokenizer tokenizer = getStackTrace(ex);
        while(tokenizer.hasMoreTokens()) {
            logger.log(level, tokenizer.nextToken());
        }
    }

    private static StringTokenizer getStackTrace(Throwable ex) {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        ex.printStackTrace(new PrintStream(temp));
        String stackTrace = new String(temp.toByteArray());
        return new StringTokenizer(stackTrace, LINE_BREAKING);
    }

    @Contract(pure = true)
    private static Logger logger() {
        return logger;
    }
}
