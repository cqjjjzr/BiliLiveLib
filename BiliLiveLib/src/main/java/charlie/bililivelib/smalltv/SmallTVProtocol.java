package charlie.bililivelib.smalltv;

import charlie.bililivelib.Globals;
import charlie.bililivelib.net.HttpHelper;

public class SmallTVProtocol {
    private final HttpHelper httpHelper;

    public SmallTVProtocol() {
        httpHelper = Globals.get().getHttpHelper();
    }


}
