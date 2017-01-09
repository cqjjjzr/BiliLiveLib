package charlie.bililivelib.util;

import org.jetbrains.annotations.Contract;

import java.awt.image.BufferedImage;

public class CaptchaUtil {
    private OCRUtil ocrUtil;
    //FOR COMMENTED CODE IN THIS CLASS: COMMENTED SHOWN A VERY LOW-PERFORMANCE IMPLEMENT WAY BY USING JAVASCRIPT.
    //private ScriptEngine scriptEngine;

    public CaptchaUtil() {
        ocrUtil = new OCRUtil();
        //scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    }

    /**
     * Calculate the calculating captcha image. Used at getting Free Silver Treasures.<br />
     * <s>It recognizes the image into string and then eval it as JavaScript. So we get the result of the formula.</s>
     * Now we directly parse the formula.
     * @param image Image to calculate captcha.
     * @return Calculated value.
     */
    @Contract(pure = true)
    public int evalCalcCaptcha(BufferedImage image) {
        String str = ocrUtil.ocrCalcCaptcha(image);
        if (str == null) return -1;
        return calc(str);
        /*try {
            return (Integer) scriptEngine.eval(str);
        } catch (ScriptException e) {
            return -1;
        }*/
    }

    private int calc(String formula) {
        formula = formula.trim();
        if (formula.contains("+")) { //ADD FORMULA
            int i1 = Integer.parseInt(formula.substring(0, formula.indexOf('+')));
            int i2 = Integer.parseInt(formula.substring(formula.indexOf('+') + 1)); // +1 FOR SKIP '+'.
            return i1 + i2;
        } else { //SUB FORMULA
            int i1 = Integer.parseInt(formula.substring(0, formula.indexOf('-')));
            int i2 = Integer.parseInt(formula.substring(formula.indexOf('-') + 1)); // +1 FOR SKIP '+'.
            return i1 - i2;
        }
    }
}
