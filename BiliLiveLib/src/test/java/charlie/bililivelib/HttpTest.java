package charlie.bililivelib;

import charlie.bililivelib.net.BilibiliTrustStrategy;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.net.PostArguments;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertTrue;

public class HttpTest {
    @Test
    public void getCaptcha() throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        httpHelper.init();
        HttpResponse response = httpHelper.createPostResponse(
                Globals.get().getBiliPassportHttpsRoot(),
                "/login/dologin",
                new PostArguments()
        );
        System.out.println(HttpHelper.responseToString(response));
    }

    @Test
    public void trust() throws Exception {
        assertTrue(new BilibiliTrustStrategy().isTrusted(new X509Certificate[]{
                (X509Certificate) CertificateFactory.getInstance("X.509")
                        .generateCertificate(new FileInputStream("bilibili.cer"))
        }, null));
    }
}
