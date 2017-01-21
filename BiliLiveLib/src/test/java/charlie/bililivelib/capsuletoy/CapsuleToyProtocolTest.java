package charlie.bililivelib.capsuletoy;

import charlie.bililivelib.TestSessionHelper;
import charlie.bililivelib.exceptions.BiliLiveException;
import lombok.Getter;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CapsuleToyProtocolTest {
    private static CapsuleToyProtocol protocol;

    @BeforeClass
    public static void init() throws IOException, BiliLiveException {
        protocol = new CapsuleToyProtocol(TestSessionHelper.initSession());
    }

    @Test
    public void openAllNormal() throws Exception {
        WrappedRequests requests = new WrappedRequests(protocol.getCapsuleToyInfo().getAvailableNormal());
        for (int i = 0; i < requests.hundred; i++) {
            System.out.println(protocol.openNormal(100));
        }
        for (int i = 0; i < requests.ten; i++) {
            System.out.println(protocol.openNormal(10));
        }
        for (int i = 0; i < requests.one; i++) {
            System.out.println(protocol.openNormal(1));
        }
    }

    @Test
    public void aGetInfo() throws Exception {
        System.out.println(protocol.getCapsuleToyInfo());
    }

    @Getter
    public static class WrappedRequests {
        private int one;
        private int ten;
        private int hundred;

        public WrappedRequests(int total) {
            one = total % 10;
            ten = (total % 100) / 10;
            hundred = total / 100;
        }
    }
}