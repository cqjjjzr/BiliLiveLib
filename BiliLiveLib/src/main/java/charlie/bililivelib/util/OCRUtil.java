package charlie.bililivelib.util;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;

public class OCRUtil {
    private Tesseract tesseract;

    public OCRUtil() {
        tesseract = new Tesseract();
        tesseract.setLanguage("captcha");
        tesseract.setPageSegMode(7);
    }

    public String ocrCalcCaptcha(BufferedImage image) {
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            return null;
        }
    }
}
