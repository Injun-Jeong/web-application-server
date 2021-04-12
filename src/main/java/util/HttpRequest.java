package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private InputStream in;
    boolean login;
    RequestLine requestLine;
    Map<String, String> header;
    Map<String, String> params;

    public HttpRequest(InputStream in) {
        this.in = in;
        login = false;
        header = new HashMap<>();
        params = new HashMap<>();
        parseMessage();
    }

    /**
     * 클라이언트 요청 데이터를 담고 있는 InputStream을 생성자로 받아 HTTP 메서드, URL, 헤더, 본문을 분리하는 작업을 한다.
     */
    private void parseMessage() {
        try {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            String line = buffer.readLine();
            log.debug("request line : {}", line);

            if ( Objects.isNull(line) ) return;

            log.debug("■ 요청 메시지 분리 시작");
            requestLine = new RequestLine(line);

            log.debug("■ 헤더 분리 시작");
            line = buffer.readLine();
            while ( !Objects.isNull(line) && !line.isEmpty() ) {
                log.debug("header: {}", line);
                String[] headerTokens = line.split(":");
                header.put(headerTokens[0].trim(), headerTokens[1].trim());
                if ( line.contains("Cookie") ) {
                    login = isLogin(line);
                }
                line = buffer.readLine();
            }

            log.debug("■ 본문 분리 시작");
            if ( requestLine.getHttpMethod().isPost() ) {
                String readData = IOUtils.readData(buffer, Integer.parseInt(getHeader("Content-Length")));
                params = HttpRequestUtils.parseQueryString(readData.trim());
            } else if ( requestLine.getHttpMethod().isGet() ) {
                params = requestLine.getParams();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean isLogin(String line) {
        String[] headerTokens = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        if ( Objects.isNull(value) ) {
            return false;
        } else {
            return Boolean.parseBoolean(value);
        }
    }


    /**
     * Getter
     */
    public HttpMethod getMethod() {
        return requestLine.getHttpMethod();
    }

    public String getPath() {
        return requestLine.getUrl();
    }

    public boolean isLogined() {
        return login;
    }

    public String getHeader(String headerKey) {
        return header.getOrDefault(headerKey, "");
    }

    public String getParameter(String paramKey) {
        return params.getOrDefault(paramKey, "");
    }
}
