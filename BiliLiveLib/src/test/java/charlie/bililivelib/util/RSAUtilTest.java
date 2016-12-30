package charlie.bililivelib.util;

import org.junit.Test;

public class RSAUtilTest {
    private static final String KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdScM09sZJqFPX7bvmB2y6i08J\n" +
                    "bHsa0v4THafPbJN9NoaZ9Djz1LmeLkVlmWx1DwgHVW+K7LVWT5FV3johacVRuV98\n" +
                    "37+RNntEK6SE82MPcl7fA++dmW2cLlAjsIIkrX+aIvvSGCuUfcWpWFy3YVDqhuHr\n" +
                    "NDjdNcaefJIQHMW+sQIDAQAB\n";

    @Test
    public void encrypt() throws Exception {
        byte[] data = "test".getBytes();

        System.out.println(RSAUtil.encrypt(data, KEY.replaceAll("\n", "")));
    }
}