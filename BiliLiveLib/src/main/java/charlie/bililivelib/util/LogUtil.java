package charlie.bililivelib.util;

import charlie.bililivelib.BiliLiveLib;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;

public class LogUtil {
    private static final String LINE_BREAKING = "\r\n";

    private static Logger logger;

    public static void init() {
        logger = LogManager.getLogger(BiliLiveLib.class);
    }

    public static synchronized void logException(Level level, String message, Throwable ex) {
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

    private static Logger logger() {
        return logger;
    }
}
