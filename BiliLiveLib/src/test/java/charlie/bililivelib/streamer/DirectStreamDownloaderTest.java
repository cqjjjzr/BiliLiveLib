package charlie.bililivelib.streamer;

import charlie.bililivelib.BiliLiveLib;
import charlie.bililivelib.event.DownloadEvent;
import charlie.bililivelib.event.DownloadListener;
import charlie.bililivelib.i18n.I18n;
import charlie.bililivelib.util.LogUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URL;

public class DirectStreamDownloaderTest {
    private static final String ADDRESS = "http://cn-cq3-dx.acgvideo.com/vg5/b/08/12352769-1.flv?expires=1481970900&ssig=VF0xLtY-fVWNsCXyl-Belw&oi=3736312299&nfa=jh1gYOK5yu5onKTDA7JgFw==&dynamic=1";

    @BeforeClass
    public static void init() {
        I18n.init();
        LogUtil.init();
    }

    @Test
    public void testNatureExit() throws Exception {
        DirectStreamDownloader downloader = new DirectStreamDownloader(new URL(ADDRESS), null,
                new File("D:\\Test.flv"));
        downloader.addDownloadListener(new TestListener());
        downloader.start();
        synchronized (downloader) {
            downloader.wait();
        }
    }

    @Test
    public void testForceExit() throws Exception {
        DirectStreamDownloader downloader = new DirectStreamDownloader(new URL(ADDRESS), null,
                new File("D:\\Test2.flv"));
        downloader.addDownloadListener(new TestListener());
        downloader.start();
        Thread.sleep(10000);
        downloader.forceStop();
    }


    @Test
    public void testManuallyExit() throws Exception {
        DirectStreamDownloader downloader = new DirectStreamDownloader(new URL(ADDRESS), null,
                new File("D:\\Test3.flv"));
        downloader.addDownloadListener(new TestListener());
        downloader.start();
        Thread.sleep(3000);
        downloader.tryStop();
        synchronized (downloader) {
            downloader.wait();
        }
    }

    private class TestListener implements DownloadListener {
        private Logger logger = LogManager.getLogger(BiliLiveLib.class);
        @Override
        public void downloadEvent(DownloadEvent event) {
            logger.log(Level.INFO, event.getMessage());
        }
    }
}