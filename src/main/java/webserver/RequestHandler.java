package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public DataBase db = new DataBase();

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest httpRequest = new HttpRequest(in);
            String url = httpRequest.getPath();

            if (url.equals("/user/create")) {
                User user = new User(httpRequest.getParameter("userId"), httpRequest.getParameter("password"), httpRequest.getParameter("name"), httpRequest.getParameter("email"));

                log.debug("User: {}", user);
                db.addUser(user);

                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "/index.html");
            }
            else if (url.equals("/user/login")) {
                String userId = httpRequest.getParameter("userId");
                String password = httpRequest.getParameter("password");

                Optional<User> getOptionalUser = db.findUserById(userId);
                if ( getOptionalUser.isPresent() && getOptionalUser.get().getPassword().equals(password) ) {
                    log.debug("Login Success");
                    DataOutputStream dos = new DataOutputStream(out);
                    response302LoginSuccessHeader(dos);
                } else {
                    log.debug("Login Fail");
                    DataOutputStream dos = new DataOutputStream(out);
                    response302Header(dos, "/user/login_failed.html");
                }
            }
            else if (url.equals("/user/list")) {
                if ( httpRequest.isLogined() ) {
                    responseResource(out, "/index.html");
                    return;
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for (User user : users) {
                    sb.append("<tr>");
                    sb.append("<td>".concat(user.getUserId()).concat("</td>"));
                    sb.append("<td>".concat(user.getName()).concat("</td>"));
                    sb.append("<td>".concat(user.getEmail()).concat("</td>"));
                    sb.append("</tr>");
                }
                sb.append("</table>");
                byte[] body = sb.toString().getBytes();
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
            else {
                responseResource(out, url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp".concat(url)).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true\r\n");
            dos.writeBytes("Location: /index.html\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
