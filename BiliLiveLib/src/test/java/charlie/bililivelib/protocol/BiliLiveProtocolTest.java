package charlie.bililivelib.protocol;

import charlie.bililivelib.Globals;
import charlie.bililivelib.exceptions.BiliLiveException;
import charlie.bililivelib.room.Room;
import charlie.bililivelib.user.Session;
import charlie.bililivelib.util.I18n;
import charlie.bililivelib.util.LogUtil;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BiliLiveProtocolTest {
    public static final int SIXTEEN_ROOM_ID = 148;
    public static final int SIXTEEN_REAL_ROOM_ID = 10313;
    public static final int INVALID_ROOM_ID = 165454948;
    public static final int SHANXING_ROOM_ID = 459985;
    private static Session session;
    private static Session invalidSession;
    private Room room;

    @BeforeClass
    public static void init() {
        I18n.init();
        LogUtil.init();
        session = new Session(Globals.get().getConnectionPool());
        CookieStore tempStore = new BasicCookieStore();
        invalidSession = new Session(createInvalidHttpClient(tempStore), tempStore);
    }

    private static HttpClient createInvalidHttpClient(CookieStore cookieStore) {
        return HttpClientBuilder.create().setProxy(new HttpHost("127.0.0.1", 33112))
                .setDefaultCookieStore(cookieStore).build();
    }

    @AfterClass
    public static void destroy() {
        System.out.println(((PoolingHttpClientConnectionManager) Globals.get().getConnectionPool()).getTotalStats());
    }

    @org.junit.Test
    public void getRealRoomID() throws Exception {
        room = new Room(SIXTEEN_ROOM_ID, session);
        assertEquals(SIXTEEN_REAL_ROOM_ID, room.getRoomID());
    }

    @org.junit.Test
    public void getRealRoomIDInvalid() throws Exception {
        try {
            room = new Room(INVALID_ROOM_ID, session);
        } catch (BiliLiveException ex){
            LogUtil.logException(Level.ERROR, "Error getting room id!", ex);
        }
    }

    @org.junit.Test
    public void getRealRoomIDNetworkError() throws Exception {
        try {
            room = new Room(SIXTEEN_ROOM_ID, invalidSession);
        } catch (BiliLiveException ex){
            LogUtil.logException(Level.ERROR, "Error getting room id!", ex);
        }
    }

    @Test
    public void getLiveAddresses() throws Exception {
        Room room = new Room(SIXTEEN_ROOM_ID, session);
        System.out.println(room.getLiveAddresses());
    }

    @org.junit.Test
    public void getRoomInfo() throws Exception {
        try {
            Room room = new Room(SHANXING_ROOM_ID, session);
            assertEquals("山新直播间", room.getRoomTitle());
            System.out.println(room);
        } catch (BiliLiveException ex){
            LogUtil.logException(Level.ERROR, "Error getting room id!", ex);
        }
    }
}