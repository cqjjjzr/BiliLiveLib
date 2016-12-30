package charlie.bililivelib;

import charlie.bililivelib.net.BilibiliTrustStrategy;
import charlie.bililivelib.net.HttpHelper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertTrue;

public class HttpTest {
    @Test
    public void getCaptcha() throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        httpHelper.init();
        HttpResponse response = httpHelper.createGetResponse(
                Globals.get().getBiliPassportHttpsRoot(),
                "/captcha"
        );
        ImageIO.write(ImageIO.read(HttpHelper.responseToInputStream(response)), "png",
                new File("captchaLogin.png"));
    }

    @Test
    public void trust() throws Exception {
        assertTrue(new BilibiliTrustStrategy().isTrusted(new X509Certificate[]{
                (X509Certificate) CertificateFactory.getInstance("X.509")
                        .generateCertificate(new FileInputStream("bilibili.cer"))
        }, null));
    }
}
