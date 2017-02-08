package charlie.bililivelib.internalutil;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CaptchaUtilTest {
    @Test
    public void evalCalcCaptcha() throws Exception {
        CaptchaUtil util = new CaptchaUtil();
        List<String> excepted = Files.readAllLines(Paths.get(
                getClass().getResource("/captcha/excepted_values.txt").toURI()));
        //PROBLEM:CAPTCHA NO.13 CAN'T WORK!
        for (int i = 0; i < 50;i++) {
            int value = util.evalCalcCaptcha(ImageIO.read(
                    getClass().getResource("/captcha/captcha" + i + ".png")));
            System.out.println(value);
            assertEquals(Integer.parseInt(excepted.get(i).trim()), value);
        }
    }
}