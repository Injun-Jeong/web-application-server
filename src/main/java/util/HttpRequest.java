package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.InputStream;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private InputStream in;

    public HttpRequest(InputStream in) {
        this.in = in;
    }

    private void parseMessage() {

    }

}
