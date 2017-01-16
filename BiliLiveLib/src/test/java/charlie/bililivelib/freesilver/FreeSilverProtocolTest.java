package charlie.bililivelib.freesilver;

import charlie.bililivelib.util.I18n;
import charlie.bililivelib.util.LogUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import static charlie.bililivelib.TestSessionHelper.initSession;
import static org.junit.Assert.fail;

public class FreeSilverProtocolTest {
    @BeforeClass
    public static void setUp() throws Exception {
        I18n.init();
        LogUtil.init();
    }

    @Test
    public void waitToGetSilver() throws Exception {
        FreeSilverProtocol protocol = new FreeSilverProtocol(initSession());
        FreeSilverProtocol.CurrentSilverTaskInfo taskInfo = protocol.getCurrentFreeSilverStatus();
        if (!taskInfo.hasRemaining()) fail();
        FreeSilverProtocol.GetSilverInfo info = null;
        do {
            try {
                info = protocol.waitToGetSilver();
                System.out.println(info);
            } catch (Exception e) {
                if (e.getMessage().contains("FINISHED"))
                    e.printStackTrace();
            }
        } while (info == null || !info.isEnd());
    }
}