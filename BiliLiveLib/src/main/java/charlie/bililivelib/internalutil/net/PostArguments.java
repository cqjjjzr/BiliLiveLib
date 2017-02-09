package charlie.bililivelib.internalutil.net;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * 用于方便构造Http Post参数的Entity的类。仅内部使用。<br />
 * 注意，本类的通常使用方式是串联使用，例如：<br />
 * {@code new PostArguments().add("username", "admin").add("password", "123abc").toEntity()}
 *
 * @author Charlie Jiang
 * @since rv1
 */
public class PostArguments {
    private List<NameValuePair> nameValuePairs = new LinkedList<>();

    /**
     * 添加一个Post参数键/值对。
     *
     * @param name  键
     * @param value 值
     * @return 本类对象以便串联调用。
     */
    public PostArguments add(String name, String value) {
        nameValuePairs.add(new BasicNameValuePair(name, value));
        return this;
    }

    /**
     * 将本类转换为Http Client可使用的Http Entity。
     *
     * @return 转换后的Entity对象。
     * @throws UnsupportedEncodingException 当默认编码不被支持时抛出
     * @see HttpHelper
     */
    public UrlEncodedFormEntity toEntity() throws UnsupportedEncodingException {
        return new UrlEncodedFormEntity(nameValuePairs);
    }
}
