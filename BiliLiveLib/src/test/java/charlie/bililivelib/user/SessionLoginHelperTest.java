package charlie.bililivelib.user;

import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.assertEquals;

public class SessionLoginHelperTest {
    @Test
    public void startLogin() throws Exception {
        String email = testInput("E-Mail:");
        String password = testInput("Password:");

        SessionLoginHelper helper = new SessionLoginHelper(email, password);
        JOptionPane.showMessageDialog(null, "Captcha", "Captcha",
                JOptionPane.PLAIN_MESSAGE, new ImageIcon(helper.getCaptcha()));

        String captcha = testInput("Captcha:");
        helper.loginWithCaptcha(captcha);

        assertEquals(SessionLoginHelper.LoginStatus.SUCCESS, helper.getStatus());
    }

    private String testInput(String message) {
        return JOptionPane.showInputDialog(null, message, "Test",
                JOptionPane.PLAIN_MESSAGE);
    }
}