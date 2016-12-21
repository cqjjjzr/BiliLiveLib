package charlie.bililivelib.smalltv;

import charlie.bililivelib.GlobalObjects;
import charlie.bililivelib.net.HttpHelper;

public class SmallTVProtocol {
    private final HttpHelper httpHelper;

    public SmallTVProtocol() {
        httpHelper = GlobalObjects.instance().getHttpHelper();
    }


}
