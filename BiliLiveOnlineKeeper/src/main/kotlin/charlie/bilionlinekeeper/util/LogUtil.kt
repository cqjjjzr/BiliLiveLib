package charlie.bilionlinekeeper.util

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*

object LogUtil {
    private val LINE_BREAKING = System.lineSeparator()

    @Synchronized fun logException(logger: Logger, level: Level, message: String, e: Throwable) {
        logger.log(level, message)

        val stackTraceStrings: StringTokenizer = toStackTrace(e)
        while (stackTraceStrings.hasMoreTokens())
            logger.log(level, stackTraceStrings.nextToken())
    }

    @Contract(pure = true)
    private fun toStackTrace(e: Throwable): StringTokenizer {
        val temp = ByteArrayOutputStream()
        e.printStackTrace(PrintStream(temp))
        return StringTokenizer(String(temp.toByteArray()), LINE_BREAKING)
    }
}