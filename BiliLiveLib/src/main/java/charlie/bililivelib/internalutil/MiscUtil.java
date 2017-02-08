package charlie.bililivelib.internalutil;

public class MiscUtil {
    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
