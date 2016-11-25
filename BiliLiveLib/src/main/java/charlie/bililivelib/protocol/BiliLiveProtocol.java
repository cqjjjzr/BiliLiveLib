package charlie.bililivelib.protocol;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.GlobalObjects;
import charlie.bililivelib.i18n.I18n;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.net.datamodel.LiveAddresses;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public class BiliLiveProtocol {
    private static final String REAL_ROOMID_GET = "/";
    private static final String LIVE_ADDRESSES_GET = "/api/playurl&player=1&quality=0&cid=";
    private static final int INVALID_ROOM_ID = -1;

    private final HttpHost BILILIVE_ROOT;
    private final HttpHelper httpHelper;
    private final Pattern REAL_ROOMID_PATTERN;

    private GlobalObjects go;

    public BiliLiveProtocol(GlobalObjects globalObjects) {
        this.go = globalObjects;

        BILILIVE_ROOT = go.getBiliLiveRoot();
        httpHelper = go.getHttpHelper();
        REAL_ROOMID_PATTERN = Pattern.compile("(?<=var ROOMID = )(\\d+)(?=;)");
    }

    public int getRealRoomID(int originalRoomID) throws BiliLiveException {
        try {
            HttpResponse response = httpHelper.doGet(BILILIVE_ROOT, getRealRoomIDRequestURL(originalRoomID));
            int statusCode = HttpHelper.getStatusCode(response);

            if (statusCode == HTTP_OK) {
                String httpString = HttpHelper.responseToString(response);
                return parseRealRoomID(httpString);
            } else if (statusCode == HTTP_NOT_FOUND) { //NOT FOUND means invalid room id.
                throw new BiliLiveException(I18n.format("exception.roomid_not_found", originalRoomID));
            }
            throw BiliLiveException.createHttpError(I18n.getString("exception.roomid"), statusCode);
        } catch (IOException ex) {
            throw BiliLiveException.createCausedException(I18n.getString("exception.roomid"), ex);
        }
    }

    private String getRealRoomIDRequestURL(int originalRoomID) {
        return REAL_ROOMID_GET + originalRoomID;
    }

    private int parseRealRoomID(String httpString) {
        Matcher matcher = REAL_ROOMID_PATTERN.matcher(httpString);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return INVALID_ROOM_ID;
    }

    public LiveAddresses getLiveAddresses(int roomID) throws BiliLiveException {
        checkRoomID(roomID);
        try {
            HttpResponse response = httpHelper.doGet(BILILIVE_ROOT, getLiveAddressesRequestURL(roomID));
            int statusCode = HttpHelper.getStatusCode(response);

            if (statusCode == HTTP_OK) {
                String httpString = HttpHelper.responseToString(response);
                return parseLiveAddresses();
            }
            throw BiliLiveException.createHttpError(I18n.getString("exception.live_addresses"), statusCode);
        } catch (IOException ex) {
            throw BiliLiveException.createCausedException(I18n.getString("exception.live_addresses"), ex);
        }
    }

    private LiveAddresses parseLiveAddresses() {
        return null;
    }

    private String getLiveAddressesRequestURL(int roomID) {
        return LIVE_ADDRESSES_GET + roomID;
    }

    private void checkRoomID(int realRoomID) throws BiliLiveException {
        if (realRoomID < 1) throw new BiliLiveException(I18n.format("exception.roomid_invalid", realRoomID));
    }
}
