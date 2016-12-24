package charlie.bililivelib.util;

import charlie.bililivelib.Globals;
import charlie.bililivelib.net.HttpHelper;
import lombok.Data;
import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Level;
import org.junit.Ignore;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OCRUtilTest {
    // FILL THESE FROM YOURSELF's COOKIE IF YOU WANT TO GET NEW CAPTCHA.
    public static final String LIVE_LOGIN_DATA = "";
    public static final String DEDE_USER_ID = "";
    public static final String SESS_DATA = "";
    public static final String CK_PV = "";
    public static final String DEDE_USER_ID_CKMD5 = "";

    @Test
    public void ocrCalcCaptcha() throws Exception {
        OCRUtil util = new OCRUtil();
        List<String> excepted = Files.readAllLines(Paths.get(
                getClass().getResource("/captcha/excepted.txt").toURI()));
        //PROBLEM:CAPTCHA NO.13 CAN'T WORK!
        for (int i = 0; i < 50;i++) {
            String str = util.ocrCalcCaptcha(ImageIO.read(
                    getClass().getResource("/captcha/captcha" + i + ".png")));
            System.out.println(str.trim());
            assertEquals(excepted.get(i).trim(), str.trim());
        }
    }

    private HttpClient forceReplaceAndReturnHttpClient(HttpClient httpClient) {
        try {
            Field clientField = Globals.get()
                    .getHttpHelper().getClass().getDeclaredField("httpClient");
            clientField.setAccessible(true);
            HttpClient original = (HttpClient) clientField.get(Globals.get().getHttpHelper());
            clientField.set(Globals.get().getHttpHelper(), httpClient);
            return original;
        } catch (Exception e) {
            LogUtil.logException(Level.ERROR, "Error replacing Http Client!", e);
        }
        return null;
    }

    @Ignore
    public void grabKey() throws Exception {
        BasicCookieStore store = new BasicCookieStore();
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(store).build();
        store.addCookie(new BCookie("LIVE_LOGIN_DATA", LIVE_LOGIN_DATA, "live.bilibili.com"));
        store.addCookie(new BCookie("DedeUserID", DEDE_USER_ID, "live.bilibili.com"));
        store.addCookie(new BCookie("SESSDATA", SESS_DATA, "live.bilibili.com"));
        store.addCookie(new BCookie("ck_pv", CK_PV, "live.bilibili.com"));
        store.addCookie(new BCookie("DedeUserID__ckMd5", DEDE_USER_ID_CKMD5, "live.bilibili.com"));
        forceReplaceAndReturnHttpClient(client);
        for(int i = 0;i < 50; i++) {
            File file = new File("/captcha/captcha" + i + ".png");
            file.mkdirs();
            if (!file.exists()) file.createNewFile();
            ImageIO.write(ImageIO.read(HttpHelper.responseToInputStream(Globals.get().getHttpHelper()
                    .createGetResponse(Globals.get().getBiliLiveRoot(), "/freeSilver/getCaptcha"))), "png",
                    file);
            System.out.println(file.getAbsolutePath());
        }
    }

    @Data
    private class BCookie implements Cookie {
        public BCookie(String name, String value, String domain) {
            this.name = name;
            this.value = value;
            this.domain = domain;
        }

        private String name;
        private String value;
        private String comment;
        private String commentURL;
        private Date expiryDate;
        private boolean persistent;
        private String domain;
        private String path;
        private int[] ports;
        private boolean secure;
        private int version;

        @Override
        public boolean isExpired(Date date) {
            return false;
        }
    }
}