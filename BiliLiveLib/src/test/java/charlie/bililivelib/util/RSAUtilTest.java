package charlie.bililivelib.util;

import org.junit.Test;

public class RSAUtilTest {
    private static final String KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdb9YxqXHlwRNWnfnT1HEAk+nO\n" +
                    "ZRWwv5PUY3TJz5l/VM2lpTlbcnXU6vnk+xLm+7MtPMSuMZHt4vgyh6uNzo9IC4LZ\n" +
                    "f+Zw5MkwidGFOFbBTPdeUP7VG3FK4leXPu2eQBslCaJ1P4Wl26OTw5kzO6vZx++a\n" +
                    "OHujoZRbgFZE86seMwIDAQAB\n";

    @Test
    public void encrypt() throws Exception {
        byte[] data = "test".getBytes();

        System.out.println(RSAUtil.encrypt(data, KEY.replaceAll("\n", "")));
    }
}