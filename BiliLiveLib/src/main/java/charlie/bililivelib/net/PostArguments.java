package charlie.bililivelib.net;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

public class PostArguments {
    private List<NameValuePair> nameValuePairs = new LinkedList<>();

    public PostArguments add(String name, String value) {
        nameValuePairs.add(new BasicNameValuePair(name, value));
        return this;
    }

    public UrlEncodedFormEntity toEntity() throws UnsupportedEncodingException {
        return new UrlEncodedFormEntity(nameValuePairs);
    }
}
