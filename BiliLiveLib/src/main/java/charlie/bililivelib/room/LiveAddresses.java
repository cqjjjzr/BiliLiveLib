package charlie.bililivelib.room;

import charlie.bililivelib.BiliLiveException;
import charlie.bililivelib.Globals;
import charlie.bililivelib.room.datamodel.ErrorResponseJson;
import charlie.bililivelib.util.I18n;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static charlie.bililivelib.util.I18n.getString;

@Data
@AllArgsConstructor
public class LiveAddresses {
    private static final String RESP_RESULT_SUCCESS = "suee";

    private String lineMain;
    private String lineBackup1;
    private String lineBackup2;
    private String lineBackup3;

    public static LiveAddresses fromXMLString(String xmlString) throws BiliLiveException {
        if (!isValidXMLDocument(xmlString))
            processInvalidLiveAddresses(xmlString);
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
            throw new BiliLiveException(getString("exception.live_addresses"), e);
        }
    }

    private static void processInvalidLiveAddresses(String xmlString) throws BiliLiveException {
        ErrorResponseJson responseJson = Globals.get().getGson().fromJson(xmlString, ErrorResponseJson.class);
        String message = generateInvalidLiveAddressesMessage(responseJson);
        throw new BiliLiveException(message);
    }

    private static String generateInvalidLiveAddressesMessage(ErrorResponseJson json) {
        return I18n.format("exception.live_addresses_invalid", json.getCode(), json.getMessage());
    }

    private static boolean isValidXMLDocument(String xmlString) {
        return xmlString.contains("<video>");
    }
}
