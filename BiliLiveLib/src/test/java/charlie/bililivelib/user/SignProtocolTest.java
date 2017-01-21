package charlie.bililivelib.user;

import charlie.bililivelib.TestSessionHelper;
import org.junit.Test;

import static org.junit.Assert.fail;

public class SignProtocolTest {
    @Test
    public void signIn() throws Exception {
        SignProtocol signProtocol = new SignProtocol(TestSessionHelper.initSession());
        SignProtocol.SignInfo signInfo = signProtocol.getCurrentSignInfo();
        if (signInfo.isSignedIn()) fail();

        System.out.println(signProtocol.signIn());
    }
}