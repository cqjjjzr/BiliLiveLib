package charlie.bililivelib.internalutil.net;

import org.apache.http.ssl.TrustStrategy;
import org.jetbrains.annotations.Contract;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 用于验证Bilibili Https协议。仅内部使用。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class BilibiliTrustStrategy implements TrustStrategy {
    //UPDATE IF BILIBILI's CERT CHANGES.
    public static final String BILIBILI_CERT_FINGERPRINT = "3F1455BB9988290A4019687C799C507A54BA143A";

    @Contract(pure = true)
    private static String digestMD5(byte[] data) {
        MessageDigest messageDigest;
        String md5Str;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.update(data);
            md5Str = new BigInteger(1, messageDigest.digest()).toString(16).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
        return md5Str;
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        for (X509Certificate certificate : chain) {
            if (digestMD5(certificate.getEncoded()).equals(BILIBILI_CERT_FINGERPRINT))
                return true;
        }
        return false;
    }
}