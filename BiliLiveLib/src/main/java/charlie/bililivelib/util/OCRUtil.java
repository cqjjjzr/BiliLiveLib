package charlie.bililivelib.util;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public class OCRUtil {
    public static final int SINGLE_LINE_MODE = 7;
    private Tesseract tesseract;

    public OCRUtil() {
        tesseract = new Tesseract();
        tesseract.setLanguage("captcha");
        tesseract.setPageSegMode(SINGLE_LINE_MODE);
    }

    @Contract(pure = true)
    public String ocrCalcCaptcha(@NotNull BufferedImage image) {
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            return null;
        }
    }
}
