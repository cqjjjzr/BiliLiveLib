package charlie.bililivelib.util;

import charlie.bililivelib.BiliLiveException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtil {
    private static final KeyFactory FACTORY;
    private static final int BLOCK_SIZE = 64;

    static {
        try {
            FACTORY = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("", e);
        }
    }

    public static String encrypt(byte[] data, String key) throws BiliLiveException {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            PublicKey publicKey = FACTORY.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            int readLength = 0;
            byte[] tempBuf = new byte[BLOCK_SIZE];
            byte[] resultBuf = new byte[0];
            while (readLength < data.length) {
                readLength += inputStream.read(tempBuf, 0, BLOCK_SIZE);
                if (inputStream.available() > BLOCK_SIZE)
                    cipher.update(tempBuf);
                else if (inputStream.available() == BLOCK_SIZE) {
                    cipher.update(tempBuf);
                    resultBuf = cipher.doFinal();
                } else //inputStream.available < BLOCK_SIZE, do final.
                    resultBuf = cipher.doFinal(tempBuf);
            }

            return new String(Base64.getEncoder().encode(resultBuf));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            throw BiliLiveException.createCausedException("Failed decrypting!", e);
        }

    }
}
