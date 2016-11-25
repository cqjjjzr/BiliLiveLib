package charlie.bililivelib.protocol;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.GlobalObjects;
import charlie.bililivelib.i18n.I18n;
import charlie.bililivelib.net.HttpHelper;
import charlie.bililivelib.net.datamodel.LiveAddresses;
import charlie.bililivelib.protocol.datamodel.ErrorResponseJson;
import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static charlie.bililivelib.i18n.I18n.getString;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public class BiliLiveProtocol {
    private static final String REAL_ROOMID_GET = "/";
    private static final String LIVE_ADDRESSES_GET = "/api/playurl&player=1&quality=0&cid=";
    private static final int INVALID_ROOM_ID = -1;
    private static final String RESP_RESULT_SUCCESS = "suee";

    private final Gson gson;

    private final HttpHost BILILIVE_ROOT;
    private final HttpHelper httpHelper;
    private final Pattern REAL_ROOMID_PATTERN;

    public BiliLiveProtocol(GlobalObjects globalObjects) {
        gson = new Gson();

        BILILIVE_ROOT = globalObjects.getBiliLiveRoot();
        httpHelper = globalObjects.getHttpHelper();
        REAL_ROOMID_PATTERN = Pattern.compile("(?<=var ROOMID = )(\\d+)(?=;)");
    }

    public int getRealRoomID(int originalRoomID) throws BiliLiveException {
        try {
            HttpResponse response = httpHelper.doGet(BILILIVE_ROOT, getRealRoomIDRequestURL(originalRoomID));
            int statusCode = HttpHelper.getStatusCode(response);

            if (statusCode == HTTP_OK) {
                String htmlString = HttpHelper.responseToString(response);
                return parseRealRoomID(htmlString);
            } else if (statusCode == HTTP_NOT_FOUND) { //NOT FOUND means invalid room id.
                throw new BiliLiveException(I18n.format("exception.roomid_not_found", originalRoomID));
            }
            throw BiliLiveException.createHttpError(getString("exception.roomid"), statusCode);
        } catch (IOException ex) {
            throw BiliLiveException.createCausedException(getString("exception.roomid"), ex);
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
                String xmlString = HttpHelper.responseToString(response);
                return parseLiveAddresses(xmlString);
            }
            throw BiliLiveException.createHttpError(getString("exception.live_addresses"), statusCode);
        } catch (IOException ex) {
            throw BiliLiveException.createCausedException(getString("exception.live_addresses"), ex);
        }
    }

    private LiveAddresses parseLiveAddresses(String xmlString) throws BiliLiveException {
        if (isValidXMLDocument(xmlString)) {
        } else {
            processInvalidLiveAddresses(xmlString);
        }
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xmlDocument = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
            Node resultMessageNode = xmlDocument.getElementsByTagName("result").item(0);
            Node mainLineURLNode   = xmlDocument.getElementsByTagName("url")   .item(0);
            Node backup1URLNode    = xmlDocument.getElementsByTagName("b1url") .item(0);
            Node backup2URLNode    = xmlDocument.getElementsByTagName("b2url") .item(0);
            Node backup3URLNode    = xmlDocument.getElementsByTagName("b3url") .item(0);
            if (!resultMessageNode.getTextContent().trim().equals(RESP_RESULT_SUCCESS))
                throw new BiliLiveException(getString("exception.live_addresses") +
                        " Result:" + resultMessageNode.getTextContent());
            if (mainLineURLNode.getTextContent().trim().isEmpty())
                throw new BiliLiveException(getString("exception.live_addresses"));
            return new LiveAddresses(
                    mainLineURLNode.getTextContent().trim(),
                    backup1URLNode .getTextContent().trim(),
                    backup2URLNode .getTextContent().trim(),
                    backup3URLNode .getTextContent().trim());
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw BiliLiveException.createCausedException(getString("exception.live_addresses"), e);
        }
    }

    private void processInvalidLiveAddresses(String xmlString) throws BiliLiveException {
        ErrorResponseJson responseJson = gson.fromJson(xmlString, ErrorResponseJson.class);
        String message = generateInvalidLiveAddressesMessage(responseJson);
        throw new BiliLiveException(message);
    }

    private String generateInvalidLiveAddressesMessage(ErrorResponseJson json) {
        return I18n.format("exception.live_addresses_invalid", json.getCode(), json.getMessage());
    }

    private boolean isValidXMLDocument(String xmlString) {
        return xmlString.contains("<video>");
    }

    private String getLiveAddressesRequestURL(int roomID) {
        return MessageFormat.format("/api/playurl?cid={0,number,###}&player=1&quality=0", roomID);
    }

    private void checkRoomID(int realRoomID) throws BiliLiveException {
        if (realRoomID < 1) throw new BiliLiveException(I18n.format("exception.roomid_invalid", realRoomID));
    }
}
