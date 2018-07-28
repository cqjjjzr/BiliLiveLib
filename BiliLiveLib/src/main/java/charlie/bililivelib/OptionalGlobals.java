package charlie.bililivelib;

import charlie.bililivelib.internalutil.OCRUtil;
import com.gargoylesoftware.htmlunit.Cache;

/**
 * 本单例类定义了一组在整个应用程序中的*可选*全局变量，这些变量仅用于Library内部使用且随时可能修改，请勿随意访问该类中的对象。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class OptionalGlobals {
    private static OptionalGlobals instance;

    private ThreadLocal<Cache> htmlUnitCache;
    private OCRUtil ocrUtil;

    public static OptionalGlobals get() {
        if (instance == null) {
            instance = new OptionalGlobals();
            instance.init();
        }
        return instance;
    }

    public void init() {
        htmlUnitCache = ThreadLocal.withInitial(Cache::new);
    }

    public Cache getHtmlUnitCache() {
        return htmlUnitCache.get();
    }

    public OCRUtil getOcrUtil() {
        if (ocrUtil == null) {
            ocrUtil = new OCRUtil();
        }
        return ocrUtil;
    }
}
