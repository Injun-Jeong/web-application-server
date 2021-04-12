package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestLine {
    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);

    private final String httpMethod;
    private Map<String, String> params;
    private String url;

    public RequestLine(String requestLine) {
        String[] tokens = requestLine.split(" ");

        httpMethod = tokens[0];
        url = tokens[1].equals("/") ? "/index.html" : tokens[1];

        if ( url.contains("?") ) {
            String[] urlTokens = url.split("\\?");
            url = urlTokens[0];
            params = HttpRequestUtils.parseQueryString(urlTokens[1].trim());
        }
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return Objects.isNull(params) ? new HashMap<>() : params;
    }
}
