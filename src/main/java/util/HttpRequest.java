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
    String httpMethod;
    String url;
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
            httpMethod = requestLine.getHttpMethod();
            url = requestLine.getUrl();

            log.debug("■ 헤더 분리 시작");
            int contentLength = 0;      // body의 크기
            line = buffer.readLine();
            while ( !Objects.isNull(line) && !line.isEmpty() ) {
                log.debug("header: {}", line);
                String[] headerTokens = getHeagerTokens(line);
                header.put(headerTokens[0].trim(), headerTokens[1].trim());

                if ( line.contains("Content-Length")) {
                    contentLength = getContentLength(line);
                }
                if ( line.contains("Cookie") ) {
                    login = isLogin(line);
                }
                line = buffer.readLine();
            }

            log.debug("■ 본문 분리 시작");
            if ( httpMethod.equals("POST") ) {
                String readData = IOUtils.readData(buffer, contentLength);
                params = HttpRequestUtils.parseQueryString(readData.trim());
            } else if ( httpMethod.equals("GET") ) {
                params = requestLine.getParams();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getContentLength(String line) {
        String[] headerTokens = getHeagerTokens(line);
        return Integer.parseInt(headerTokens[1].trim());
    }

    private boolean isLogin(String line) {
        String[] headerTokens = getHeagerTokens(line);
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        if ( Objects.isNull(value) ) {
            return false;
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    private String[] getHeagerTokens(String line) {
        return line.split(":");
    }


    /**
     * Getter
     */
    public String getMethod() {
        return httpMethod;
    }

    public String getPath() {
        return url;
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
